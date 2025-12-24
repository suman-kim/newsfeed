package com.suman.newsfeed.presentation.dto.response;

import com.suman.newsfeed.domain.user.UserNewsPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNewsPlatformResponse {

    private List<String> platform;

}