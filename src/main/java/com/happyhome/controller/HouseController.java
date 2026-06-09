package com.happyhome.controller;

import com.happyhome.config.OpenApiProperties;
import com.happyhome.dto.HouseDeal;
import com.happyhome.dto.HouseSearchCondition;
import com.happyhome.dto.MemberDto;
import com.happyhome.service.FavoriteDealService;
import com.happyhome.service.HouseDealService;
import com.happyhome.service.RegionService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HouseController {

    private final HouseDealService houseDealService;
    private final RegionService regionService;
    private final OpenApiProperties openApiProperties;
    private final FavoriteDealService favoriteDealService;

    public HouseController(
            HouseDealService houseDealService,
            RegionService regionService,
            OpenApiProperties openApiProperties,
            FavoriteDealService favoriteDealService
    ) {
        this.houseDealService = houseDealService;
        this.regionService = regionService;
        this.openApiProperties = openApiProperties;
        this.favoriteDealService = favoriteDealService;
    }

    @GetMapping({"/prices", "/trades"})
    public String trades(@ModelAttribute("condition") HouseSearchCondition condition, Model model, HttpSession session) {
        List<HouseDeal> trades = houseDealService.search(condition);
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        Set<Integer> favoriteDealNos = loginMember == null
                ? Set.of()
                : favoriteDealService.findFavoriteDealNos(loginMember.getUserId(), 50).stream().collect(Collectors.toSet());
        model.addAttribute("trades", trades);
        model.addAttribute("favoriteDealNos", favoriteDealNos);
        model.addAttribute("sidos", regionService.findSidos());
        model.addAttribute("guguns", regionService.findGuguns(condition.getSidoName()));
        model.addAttribute("dongs", regionService.findDongs(condition.getSidoName(), condition.getGugunName()));
        model.addAttribute("kakaoKey", openApiProperties.getKakao().getJavascriptKey());
        return "prices";
    }
}
