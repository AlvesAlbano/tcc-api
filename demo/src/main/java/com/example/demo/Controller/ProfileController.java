package com.example.demo.Controller;

import com.example.demo.Model.UserProfileDTO;
import com.example.demo.Service.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
@Tag(name="Profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public UserProfileDTO getProfile(@PathVariable String steamId) throws JsonProcessingException {
        return profileService.buildProfile(steamId);
    }
}
