package com.yeoyeo.controller.webrest;

import com.yeoyeo.adapter.controller.WebRestController;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.assertThat;

public class WebRestControllerUnitTest {

    @Test
    public void read_real_profile() {
        // given
        String expectedProfile = "real";
        MockEnvironment env = new MockEnvironment();
        env.addActiveProfile(expectedProfile);
        env.addActiveProfile("real-db");
        WebRestController controller = new WebRestController(env);

        // when
        String profile = controller.profile();

        // then
        assertThat(profile).isEqualTo(expectedProfile);
    }

    @Test
    public void read_first_profile_when_real_profile_is_empty() {
        // given
        String expectedProfile = "real-db";
        MockEnvironment env = new MockEnvironment();
        env.addActiveProfile(expectedProfile);
        WebRestController controller = new WebRestController(env);

        // when
        String profile = controller.profile();

        // then
        assertThat(profile).isEqualTo(expectedProfile);
    }

    @Test
    public void read_default_profile_when_active_profile_is_empty() {
        // given
        String expectedProfile = "default";
        MockEnvironment env = new MockEnvironment();
        WebRestController controller = new WebRestController(env);

        // when
        String profile = controller.profile();

        // then
        assertThat(profile).isEqualTo(expectedProfile);
    }
}
