/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.app;

import com.epion_t3.core.common.type.ExitCode;
import com.epion_t3.devtools.bean.DevGeneratorContext;
import com.epion_t3.devtools.bean.ExecuteOptions;
import com.epion_t3.devtools.component.DocumentGenerateComponent;
import com.epion_t3.devtools.component.MessageGenerateComponent;
import com.epion_t3.devtools.component.SpecParseComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

/**
 * 開発ツール・自動生成
 *
 * @author takashno
 */
@Slf4j
public class Application {

    /**
     * オプション.
     */
    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addRequiredOption("t", "target", true, "target spec file.");
        OPTIONS.addOption("m", "message-output", true, "messages.properties generate place.");
        OPTIONS.addOption("j", "java-output", true, "enum java generate place.");
        OPTIONS.addOption("d", "doc-output", true, "document markdown generate place.");
    }

    /**
     * メイン処理.
     *
     * @param args 引数
     */
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

        // messages.properties、Messages.javaの出力
        if (cmd.hasOption("m") && cmd.hasOption("j")) {
            MessageGenerateComponent.getInstance().execute(context);
        } else {
            log.info("Skip Generate messages.properties and Messages.java");
        }

        // ドキュメントの出力
        if (cmd.hasOption("d")) {
            DocumentGenerateComponent.getInstance().execute(context);
        } else {
            log.info("Skip Generate Documents.");
        }

        System.exit(ExitCode.NORMAL.getExitCode());

    }

}
