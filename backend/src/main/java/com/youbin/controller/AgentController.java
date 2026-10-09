package com.youbin.controller;

import com.youbin.agent.GoodsAgentService;
import com.youbin.agent.SearchAgentService;
import com.youbin.common.PageResult;
import com.youbin.common.Result;
import com.youbin.dto.AgentGenerateDTO;
import com.youbin.dto.AgentGoodsResult;
import com.youbin.dto.AgentSearchDTO;
import com.youbin.vo.GoodsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final GoodsAgentService goodsAgentService;
    private final SearchAgentService searchAgentService;

    @PostMapping("/generate-goods")
    public Result<AgentGoodsResult> generateGoods(@Valid @RequestBody AgentGenerateDTO dto) {
        return Result.ok(goodsAgentService.generate(dto.getText()));
    }

    @PostMapping("/search")
    public Result<PageResult<GoodsVO>> search(@Valid @RequestBody AgentSearchDTO dto) {
        return Result.ok(searchAgentService.search(dto.getQuery(), dto.getPageNum(), dto.getPageSize()));
    }
}