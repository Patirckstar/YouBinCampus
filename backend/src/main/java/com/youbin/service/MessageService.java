package com.youbin.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.youbin.common.BizException;
import com.youbin.common.ResultCode;
import com.youbin.dto.SendMessageDTO;
import com.youbin.entity.Goods;
import com.youbin.entity.Message;
import com.youbin.entity.User;
import com.youbin.mapper.GoodsMapper;
import com.youbin.mapper.MessageMapper;
import com.youbin.mapper.UserMapper;
import com.youbin.vo.ConversationVO;
import com.youbin.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    /** 当前用户所有会话（按最后一条消息时间倒序） */
    public List<ConversationVO> conversations(Long me) {
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUser, me).or().eq(Message::getToUser, me))
                .orderByDesc(Message::getId));
        if (list.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, ConversationVO> map = new LinkedHashMap<>();
        for (Message m : list) {
            Long peerId = me.equals(m.getFromUser()) ? m.getToUser() : m.getFromUser();
            if (peerId == null) {
                continue;
            }
            String key = (m.getGoodsId() == null ? "null" : m.getGoodsId()) + "_" + peerId;
            ConversationVO vo = map.get(key);
            if (vo == null) {
                vo = new ConversationVO();
                vo.setPeerId(peerId);
                vo.setGoodsId(m.getGoodsId());
                vo.setLastContent(m.getContent());
                vo.setLastTime(m.getCreateTime());
                vo.setUnreadCount(0);
                map.put(key, vo);
            }
            if (me.equals(m.getToUser()) && Integer.valueOf(0).equals(m.getIsRead())) {
                vo.setUnreadCount(vo.getUnreadCount() + 1);
            }
        }

        Set<Long> peerIds = map.values().stream()
                .map(ConversationVO::getPeerId).collect(Collectors.toSet());
        Set<Long> goodsIds = map.values().stream()
                .map(ConversationVO::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, User> users = peerIds.isEmpty() ? new HashMap<>()
                : userMapper.selectBatchIds(peerIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Goods> goods = goodsIds.isEmpty() ? new HashMap<>()
                : goodsMapper.selectBatchIds(goodsIds).stream()
                        .collect(Collectors.toMap(Goods::getId, g -> g));

        for (ConversationVO vo : map.values()) {
            User peer = users.get(vo.getPeerId());
            if (peer != null) {
                vo.setPeerNickname(peer.getNickname());
                vo.setPeerAvatar(peer.getAvatar());
            }
            Goods g = goods.get(vo.getGoodsId());
            if (g != null) {
                vo.setGoodsTitle(g.getTitle());
                vo.setGoodsImage(firstImage(g.getImages()));
            }
        }

        List<ConversationVO> result = new ArrayList<>(map.values());
        result.sort((a, b) -> {
            LocalDateTime ta = a.getLastTime();
            LocalDateTime tb = b.getLastTime();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return result;
    }

    /** 与指定商品、指定对端的消息明细（并标记对端发来消息为已读） */
    public List<MessageVO> with(Long me, Long goodsId, Long peerId) {
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getGoodsId, goodsId)
                .and(w -> w
                        .and(x -> x.eq(Message::getFromUser, me).eq(Message::getToUser, peerId))
                        .or(y -> y.eq(Message::getFromUser, peerId).eq(Message::getToUser, me)))
                .orderByAsc(Message::getId));

        List<MessageVO> result = list.stream().map(this::toVO).collect(Collectors.toList());

        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getGoodsId, goodsId)
                .eq(Message::getFromUser, peerId)
                .eq(Message::getToUser, me)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));

        return result;
    }

    /** 发送消息 */
    public MessageVO send(Long me, SendMessageDTO dto) {
        String content = dto.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "消息内容不能为空");
        }
        Goods goods = dto.getGoodsId() == null ? null : goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (dto.getToUser() == null || userMapper.selectById(dto.getToUser()) == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "对方不存在");
        }
        if (dto.getToUser().equals(me)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "不能给自己发消息");
        }
        if (!me.equals(goods.getUserId()) && !dto.getToUser().equals(goods.getUserId())) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "只能与该商品的买家或卖家交流");
        }

        Message m = new Message();
        m.setFromUser(me);
        m.setToUser(dto.getToUser());
        m.setGoodsId(dto.getGoodsId());
        m.setContent(content.trim());
        m.setIsRead(0);
        messageMapper.insert(m);
        return toVO(messageMapper.selectById(m.getId()));
    }

    /** 未读消息数 */
    public int unreadCount(Long me) {
        Long c = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUser, me)
                .eq(Message::getIsRead, 0));
        return c == null ? 0 : c.intValue();
    }

    private MessageVO toVO(Message m) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setFromUserId(m.getFromUser());
        vo.setToUserId(m.getToUser());
        vo.setContent(m.getContent());
        vo.setIsRead(m.getIsRead());
        vo.setCreateTime(m.getCreateTime());
        return vo;
    }

    /** 从 JSON 数组字符串取第一张图片，为空则返回 null */
    private String firstImage(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            List<String> images = JSONUtil.toList(json, String.class);
            return (images == null || images.isEmpty()) ? null : images.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}