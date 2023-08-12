package com.eatza.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.eatza.order.config.JwtFilter;
import com.eatza.order.util.JwtTokenUtil;

@ExtendWith(MockitoExtension.class)
class OrderingserviceApplicationTest {
	
	@Mock
	OrderingserviceApplication orderingserviceApplication;

	JwtTokenUtil tokenUtil;
	
	@Test
	void contextLoads() {
		orderingserviceApplication = new OrderingserviceApplication();
		
		FilterRegistrationBean<JwtFilter> registrationBean = 
				orderingserviceApplication.filterRegistration(tokenUtil);
		assertThat(registrationBean.getUrlPatterns().equals(Arrays.array("/order/*")));
	}

}
