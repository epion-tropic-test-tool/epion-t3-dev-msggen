package com.epion_t3.dev.tools.messages.generator.app;

import com.epion_t3.core.common.type.ExitCode;
import com.epion_t3.dev.tools.messages.generator.bean.DevGeneratorContext;
import com.epion_t3.dev.tools.messages.generator.bean.ExecuteOptions;
import com.epion_t3.dev.tools.messages.generator.component.DocumentGenerateComponent;
import com.epion_t3.dev.tools.messages.generator.component.MessageGenerateComponent;
import com.epion_t3.dev.tools.messages.generator.component.SpecParseComponent;
import org.apache.commons.cli.*;

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

        DevGeneratorContext context = new DevGeneratorContext();
        context.setExecuteOptions(executeOptions);

        // 設計解析
        SpecParseComponent.getInstance().execute(context);

        // messages.propertiesの出力
        MessageGenerateComponent.getInstance().execute(context);

        // ドキュメントの出力
        DocumentGenerateComponent.getInstance().execute(context);

        System.exit(ExitCode.NORMAL.getExitCode());

    }

}
