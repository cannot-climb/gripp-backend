package kr.njw.gripp.user.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FindLeaderBoardAppResponse {
    private boolean success;
    private List<FindUserAppResponse> topBoard = new ArrayList<>();
    private List<FindUserAppResponse> defaultBoard = new ArrayList<>();
}
