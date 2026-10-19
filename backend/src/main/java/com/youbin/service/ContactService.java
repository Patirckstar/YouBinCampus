package com.youbin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youbin.common.BizException;
import com.youbin.common.ResultCode;
import com.youbin.entity.Goods;
import com.youbin.entity.Message;
import com.youbin.entity.User;
import com.youbin.mapper.GoodsMapper;
import com.youbin.mapper.MessageMapper;
import com.youbin.mapper.UserMapper;
import com.youbin.vo.ContactRecordVO;
import com.youbin.vo.ContactVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    /** 联系意向的固定消息内容 */
    private static final String CONTACT_CONTENT = "对你的商品表达了联系意向";

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;

    /** 商品详情页联系方式：卖家本人不可查看 */
    public ContactVO contactInfo(Long userId, Long goodsId) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (g.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "不能联系自己发布的商品");
        }
        User seller = userMapper.selectById(g.getUserId());
        ContactVO vo = new ContactVO();
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setPhone(seller.getPhone());
        }
        return vo;
    }

    /** 记录一次联系意向（写一条 message 记录） */
    public void recordContact(Long userId, Long goodsId) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (g.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        Message m = new Message();
        m.setFromUser(userId);
        m.setToUser(g.getUserId());
        m.setGoodsId(goodsId);
        m.setContent(CONTACT_CONTENT);
        m.setIsRead(0);
        messageMapper.insert(m);
    }

    /** 某商品的联系记录（仅商品卖家可调） */
    public List<ContactRecordVO> listRecords(Long userId, Long goodsId) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!g.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        List<Message> list = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getGoodsId, goodsId)
                .orderByDesc(Message::getId));
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, User> users = userMapper.selectBatchIds(
                        list.stream().map(Message::getFromUser).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        return list.stream().map(m -> {
            ContactRecordVO vo = new ContactRecordVO();
            vo.setId(m.getId());
            vo.setFromUserId(m.getFromUser());
            User u = users.get(m.getFromUser());
            if (u != null) {
                vo.setFromNickname(u.getNickname());
                vo.setFromAvatar(u.getAvatar());
            }
            vo.setContent(m.getContent());
            vo.setIsRead(m.getIsRead());
            vo.setCreateTime(m.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }
}
