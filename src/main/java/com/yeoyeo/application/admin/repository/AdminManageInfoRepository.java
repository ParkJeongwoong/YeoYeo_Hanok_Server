package com.yeoyeo.application.admin.repository;

import com.yeoyeo.domain.Admin.AdminManageInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AdminManageInfoRepository extends JpaRepository<AdminManageInfo, Long> {
    List<AdminManageInfo> findAllByOrderByCheckinAscRoom_Id();
    List<AdminManageInfo> findAllByCheckoutGreaterThanOrderByCheckinAscRoom_Id(LocalDate checkout);
    List<AdminManageInfo> findAllByCheckinAndActivated(LocalDate checkin, boolean activated);
    AdminManageInfo findByCheckinAndRoom_IdAndActivated(LocalDate checkin, long roomId, boolean activated);
}
