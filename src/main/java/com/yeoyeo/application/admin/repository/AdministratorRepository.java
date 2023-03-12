package com.yeoyeo.application.admin.repository;

import com.yeoyeo.domain.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorRepository extends JpaRepository<Administrator, String> {
}
