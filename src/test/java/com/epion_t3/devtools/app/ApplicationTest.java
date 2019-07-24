package com.epion_t3.devtools.app;

public class ApplicationTest {

    public static void main(String[] args) {

        Application app = new Application();
        app.main(new String[]{
                "-t",
                "../epion-t3-dev-msggen/src/test/resources/et3_basic_spec_config.yaml"});

    }

}
