package com.epion_t3.dev.tools.messages.generator.app;

import com.epion_t3.core.common.bean.spec.Content;
import com.epion_t3.core.common.bean.spec.ET3Spec;
import com.epion_t3.core.common.bean.spec.Message;
import com.epion_t3.core.common.type.ExitCode;
import com.epion_t3.dev.tools.messages.generator.bean.ExecuteOptions;
import com.epion_t3.dev.tools.messages.generator.bean.Property;
import com.epion_t3.dev.tools.messages.generator.component.DocumentGenerateComponent;
import com.epion_t3.dev.tools.messages.generator.component.MessagePropertyGenerateComponent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.zomu.t.lib.java.generate.common.context.ConvertContext;
import com.zomu.t.lib.java.generate.common.context.ConvertTarget;
import com.zomu.t.lib.java.generate.common.type.DefaultTemplate;
import com.zomu.t.lib.java.generate.java8.converter.Java8Converter;
import com.zomu.t.lib.java.generate.java8.model.ClassModel;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;

public class Application {

    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("t", "target", true, "target spec file.");
        OPTIONS.addOption("m", "message-output", true, "messages.properties generate place.");
        OPTIONS.addOption("j", "java-output", true, "enum java generate place.");
        OPTIONS.addOption("d", "doc-output", true, "document markdown generate place.");
    }


    public static void main(String[] args) {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        // 初期化オプションがあれば初期化処理を呼び出す
        try {
            cmd = parser.parse(OPTIONS, args, true);
        } catch (ParseException e) {
            System.err.println("Args Error...");
            e.printStackTrace(System.err);
            System.exit(ExitCode.ERROR.getExitCode());
        }

        ExecuteOptions executeOptions = new ExecuteOptions();
        executeOptions.setTarget(cmd.getOptionValue("t"));
        executeOptions.setMessageOutput(cmd.getOptionValue("m"));
        executeOptions.setJavaOutput(cmd.getOptionValue("j"));
        executeOptions.setDocOutput(cmd.getOptionValue("d"));

        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);

        // 知らない要素は無視する
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            ET3Spec et3Spec = objectMapper.readValue(Paths.get("/Users/takashimanozomu/IdeaProjects/epion-t3/submodules/epion-t3-basic/src/main/resources/et3_basic_spec_config.yaml").toFile(), ET3Spec.class);

            // _messages.properties￿を作成
            MessagePropertyGenerateComponent.getInstance().generate(et3Spec, executeOptions);

            // ￿document￿を作成
            DocumentGenerateComponent.getInstance().generate(et3Spec, executeOptions);


        } catch (
                IOException e) {
            e.printStackTrace();
        }

        System.exit(ExitCode.NORMAL.getExitCode());

    }

}
