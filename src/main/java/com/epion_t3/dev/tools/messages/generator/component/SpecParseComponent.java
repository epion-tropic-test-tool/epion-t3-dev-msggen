package com.epion_t3.dev.tools.messages.generator.component;

import com.epion_t3.core.common.bean.spec.*;
import com.epion_t3.core.common.type.StructureType;
import com.epion_t3.dev.tools.messages.generator.bean.CommandModel;
import com.epion_t3.dev.tools.messages.generator.bean.DevGeneratorContext;
import com.epion_t3.dev.tools.messages.generator.bean.FunctionModel;
import com.epion_t3.dev.tools.messages.generator.bean.Property;
import com.epion_t3.dev.tools.messages.generator.comparator.FunctionComparator;
import com.epion_t3.dev.tools.messages.generator.comparator.StructureComparator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 設計解析コンポーネント.
 *
 * @author takashno
 */
public final class SpecParseComponent implements Component {

    /**
     * シングルトンインスタンス.
     */
    private static final SpecParseComponent instance = new SpecParseComponent();

    /**
     * プライベートコンストラクタ.
     */
    private SpecParseComponent() {
        // Do Nothing...
    }

    /**
     * シングルトンインスタンスを取得.
     *
     * @return シングルトンインスタンス
     */
    public static SpecParseComponent getInstance() {
        return instance;
    }

    /**
     * 設計情報を解析.
     *
     * @param context コンテキスト
     */
    @Override
    public void execute(DevGeneratorContext context) {

        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper objectMapper = new ObjectMapper(yamlFactory);

        // 知らない要素は無視する
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            ET3Spec spec = objectMapper.readValue(Paths.get("/Users/takashimanozomu/IdeaProjects/epion-t3-basic/src/main/resources/et3_basic_spec_config.yaml").toFile(), ET3Spec.class);
            context.setSpec(spec);

            // 対象のLocaleについてコマンド出力モデルを作成
            spec.getLanguages().stream().forEach(x -> {
                Locale locale = Locale.forLanguageTag(x);
                FunctionModel fom = new FunctionModel();
                fom.setLabelName(spec.getInfo().getLabelName());
                fom.setName(spec.getInfo().getName());
                fom.setCustomPackage(spec.getInfo().getCustomPackage());
                fom.setLocale(locale);
                context.getFunctionModelMap().put(x, fom);
            });

            // 機能情報を解析
            parseInfo(context);

            // メッセージを解析
            parseMessages(context);

            // コマンドを解析
            parseCommands(context);


        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 機能情報を解析.
     *
     * @param context コンテキスト
     */
    private void parseInfo(DevGeneratorContext context) {

        context.getSpec().getInfo().getSummary().stream()
                .forEach(x -> {
                    if (context.getFunctionModelMap().containsKey(x.getLang())) {
                        context.getFunctionModelMap().get(x.getLang())
                                .addSummary(x.getContents());
                    }
                });
        context.getSpec().getInfo().getDescription().stream()
                .forEach(x -> {
                    if (context.getFunctionModelMap().containsKey(x.getLang())) {
                        context.getFunctionModelMap().get(x.getLang())
                                .addDescription(x.getContents());
                    }
                });

    }

    /**
     * メッセージ定義を解析.
     *
     * @param context コンテキスト
     */
    private void parseMessages(DevGeneratorContext context) {

        for (Message message : context.getSpec().getMessages()) {

            // メッセージ＝Propertiesファイルの１行を作成する
            Property property = new Property();
            property.setKey(message.getId());

            // Locale毎に分割されたFunctionModelに対してメッセージを追加していく
            context.getFunctionModelMap().forEach((k, v) -> {
                v.getMessages().put(message.getId(), SerializationUtils.clone(property));
            });

            // Locale毎にメッセージ文字列を設定していく.
            message.getMessage().stream().forEach(x -> {
                if (context.getFunctionModelMap().containsKey(x.getLang())) {
                    context.getFunctionModelMap().get(x.getLang())
                            .getMessages().get(message.getId())
                            .setValue(x.getContents());
                }
            });

        }

    }


    /**
     * コマンド定義を解析.
     *
     * @param context 機能出力モデルマップ
     */
    private void parseCommands(DevGeneratorContext context) {

        // Command
        for (Command command : context.getSpec().getCommands()) {

            // Localeが関係ないものはここで処理する.
            CommandModel com = new CommandModel();
            com.setId(command.getId());
            // Command Kind
            com.setAssertCommand(command.getAssertCommand());
            com.setEvidenceCommand(command.getEvidenceCommand());

            context.getFunctionModelMap().forEach((k, v) -> {
                v.getCommands().put(com.getId(), SerializationUtils.clone(com));
            });

            // Summary
            command.getSummary().stream()
                    .forEach(x -> {
                        if (context.getFunctionModelMap().containsKey(x.getLang())) {
                            context.getFunctionModelMap().get(x.getLang())
                                    .getCommand(command.getId())
                                    .addSummary(x.getContents());
                        }
                    });

            // Functions
            command.getFunction().stream()
                    .sorted(FunctionComparator.getInstance())
                    .forEach(x -> {
                        x.getSummary().forEach(y -> {
                            if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                context.getFunctionModelMap().get(y.getLang())
                                        .getCommand(command.getId())
                                        .addFunction(y.getContents());
                            }
                        });
                    });

            // Structure Description
            command.getStructure().stream()
                    .sorted(StructureComparator.getInstance())
                    .forEach(x -> {
                        if (CollectionUtils.isNotEmpty(x.getDescription())) {
                            x.getDescription().forEach(y -> {
                                if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                    context.getFunctionModelMap().get(y.getLang())
                                            .getCommand(command.getId())
                                            .addStructureDescription(y.getContents());
                                }
                            });
                        }
                    });

            // YAML構成を作成
            createCommandStructure(context, command);
        }
    }

    /**
     * YAML構成を作成.
     *
     * @param context コンテキスト
     * @param command コマンド
     */
    private void createCommandStructure(DevGeneratorContext context, Command command) {
        for (Map.Entry<String, FunctionModel> entry : context.getFunctionModelMap().entrySet()) {
            StringBuilder sb = new StringBuilder();
            createCommandStructureRecursive(entry.getKey(), sb, 0, command.getStructure());
            entry.getValue().getCommand(command.getId()).setStructure(sb.toString());
            System.out.println(sb.toString());
        }
    }

    private void createCommandStructureRecursive(String locale, StringBuilder sb, int level, List<Structure> structures) {

        if (level == 0) {
            sb.append("commands : \n");
        }

        String levelSpace = getNestString(level);

        for (Structure structure : structures) {
            sb.append(levelSpace);
            sb.append(structure.getName());
            sb.append(" : ");

            for (Content content : structure.getSummary()) {
                if (content.getLang().equals(locale)) {
                    sb.append(content.getContents().replaceAll("\\\\", ""));
                }
            }

            sb.append("\n");

            StructureType structureType = StructureType.valueOfByValue(structure.getType());
            if (structureType == StructureType.OBJECT) {
                createCommandStructureRecursive(locale, sb, level + 1, structure.getProperty());
            }

        }
    }

    /**
     * 階層に応じたYAMLのネストスペース文字列を取得.
     *
     * @param level 階層
     * @return ネストスペース文字列
     */
    private String getNestString(int level) {
        StringBuilder levelSb = new StringBuilder();
        for (int i = 0; i < level + 1; i++) {
            levelSb.append("  ");
        }
        String levelSpace = levelSb.toString();
        return levelSpace;
    }
}


