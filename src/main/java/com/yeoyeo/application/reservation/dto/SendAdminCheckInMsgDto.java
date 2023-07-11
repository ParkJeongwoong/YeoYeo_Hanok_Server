package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Admin.AdminManageInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class SendAdminCheckInMsgDto {

    List<AdminManageInfo> guestInfos;

    public SendAdminCheckInMsgDto(List<AdminManageInfo> guestInfos) {
        this.guestInfos = guestInfos;
    }

    public boolean validationCheck() {
        return getSize() != 0;
    }

    public int getSize() {
        return this.guestInfos.size();
    }

}
