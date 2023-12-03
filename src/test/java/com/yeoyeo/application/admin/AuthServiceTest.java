package com.yeoyeo.application.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.yeoyeo.application.admin.dto.SignupDto;
import com.yeoyeo.application.admin.repository.AdministratorRepository;
import com.yeoyeo.application.admin.service.AuthService;
import com.yeoyeo.domain.Admin.Administrator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // BeforeAll 어노테이션을 non-static으로 사용하기 위한 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthServiceTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private AdministratorRepository administratorRepository;

	@Test
	@Transactional
	public void test_signup() {
		// given
		String id = "test";
		String password = "test";
		String name = "test";
		SignupDto signupDto = new SignupDto(id, password, name, "");

		// when
		authService.signup(signupDto);

		// then
		Administrator administrator = administratorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
		assertThat(administrator.getId()).isEqualTo(id);
		assertThat(administrator.getName()).isEqualTo(name);
	}

}
