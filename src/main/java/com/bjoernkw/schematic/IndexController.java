package com.bjoernkw.schematic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class IndexController {

    @Value("${schematic.path:schematic}")
    private String path;

    @GetMapping
    public String redirect(RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("path", path);

        return "redirect:/{path}/tables";
    }
}
