/* Copyright (c) 2017-2020 Nozomu Takashima. */
package com.epion_t3.devtools.component;

import com.epion_t3.core.common.bean.spec.*;
import com.epion_t3.core.common.type.StructureType;
import com.epion_t3.devtools.bean.CommandModel;
import com.epion_t3.devtools.bean.ConfigurationModel;
import com.epion_t3.devtools.bean.DevGeneratorContext;
import com.epion_t3.devtools.bean.FlowModel;
import com.epion_t3.devtools.bean.FunctionModel;
import com.epion_t3.devtools.bean.Property;
import com.epion_t3.devtools.comparator.DescriptionComparator;
import com.epion_t3.devtools.comparator.FunctionComparator;
import com.epion_t3.devtools.comparator.StructureComparator;
import com.epion_t3.devtools.exception.GeneratorException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
            Path specFile = Paths.get(context.getExecuteOptions().getTarget());
            if (Files.notExists(specFile)) {
                throw new GeneratorException("fail read spec file. path:" + specFile.toString());
            }

            ET3Spec spec = objectMapper.readValue(specFile.toFile(), ET3Spec.class);
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

            // Flowを解析
            parseFlows(context);

            // コマンドを解析
            parseCommands(context);

            // 設定を解析
            parseConfigurations(context);

        } catch (IOException e) {
            throw new GeneratorException("fail parse spec file.", e);
        }

    }

    /**
     * 機能情報を解析.
     *
     * @param context コンテキスト
     */
    private void parseInfo(DevGeneratorContext context) {

        context.getSpec().getInfo().getSummary().stream().forEach(x -> {
            if (context.getFunctionModelMap().containsKey(x.getLang())) {
                context.getFunctionModelMap().get(x.getLang()).addSummary(x.getContents());
            }
        });
        context.getSpec().getInfo().getDescription().stream().forEach(x -> {
            if (context.getFunctionModelMap().containsKey(x.getLang())) {
                context.getFunctionModelMap().get(x.getLang()).addDescription(x.getContents());
            }
        });

    }

    /**
     * メッセージ定義を解析.
     *
     * @param context コンテキスト
     */
    private void parseMessages(DevGeneratorContext context) {

        Optional.ofNullable(context.getSpec().getMessages())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(message -> {

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
                            context.getFunctionModelMap()
                                    .get(x.getLang())
                                    .getMessages()
                                    .get(message.getId())
                                    .setValue(x.getContents());
                        }
                    });
                });
    }

    /**
     * Flow定義を解析.
     *
     * @param context 機能出力モデルマップ
     */
    private void parseFlows(DevGeneratorContext context) {

        // Command
        Optional.ofNullable(context.getSpec().getFlows())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(flow -> {

                    // Localeが関係ないものはここで処理する.
                    FlowModel f = new FlowModel();
                    f.setId(flow.getId());

                    context.getFunctionModelMap().forEach((k, v) -> {
                        v.getFlows().put(f.getId(), SerializationUtils.clone(f));
                    });

                    // Summary
                    flow.getSummary().stream().forEach(x -> {
                        if (context.getFunctionModelMap().containsKey(x.getLang())) {
                            context.getFunctionModelMap()
                                    .get(x.getLang())
                                    .getFlow(flow.getId())
                                    .addSummary(x.getContents());
                        }
                    });

                    // Functions
                    flow.getFunction().stream().sorted(FunctionComparator.getInstance()).forEach(x -> {
                        x.getSummary().forEach(y -> {
                            if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                context.getFunctionModelMap()
                                        .get(y.getLang())
                                        .getFlow(flow.getId())
                                        .addFunction(y.getContents());
                            }
                        });
                    });

                    // Structure Description
                    flow.getStructure().stream().sorted(StructureComparator.getInstance()).forEach(x -> {
                        if (CollectionUtils.isNotEmpty(x.getDescription())) {
                            x.getDescription().forEach(y -> {
                                if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                    context.getFunctionModelMap()
                                            .get(y.getLang())
                                            .getFlow(flow.getId())
                                            .addStructureDescription(y.getContents());
                                }
                            });
                        }
                    });

                    // YAML構成を作成
                    createStructure(context, flow);
                });
    }

    /**
     * コマンド定義を解析.
     *
     * @param context 機能出力モデルマップ
     */
    private void parseCommands(DevGeneratorContext context) {

        // Command
        Optional.ofNullable(context.getSpec().getCommands())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(command -> {

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
                    Optional.ofNullable(command.getSummary())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .forEach(x -> {
                                if (context.getFunctionModelMap().containsKey(x.getLang())) {
                                    context.getFunctionModelMap()
                                            .get(x.getLang())
                                            .getCommand(command.getId())
                                            .addSummary(x.getContents());
                                }
                            });

                    // Functions
                    Optional.ofNullable(command.getFunction())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .sorted(FunctionComparator.getInstance())
                            .forEach(x -> {
                                x.getSummary().forEach(y -> {
                                    if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                        context.getFunctionModelMap()
                                                .get(y.getLang())
                                                .getCommand(command.getId())
                                                .addFunction(y.getContents());
                                    }
                                });
                            });

                    // Structure Description
                    Optional.ofNullable(command.getStructure())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .sorted(StructureComparator.getInstance())
                            .forEach(x -> {
                                if (CollectionUtils.isNotEmpty(x.getDescription())) {
                                    x.getDescription().forEach(y -> {
                                        if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                            context.getFunctionModelMap()
                                                    .get(y.getLang())
                                                    .getCommand(command.getId())
                                                    .addStructureDescription(y.getContents());
                                        }
                                    });
                                }
                            });

                    // YAML構成を作成
                    createStructure(context, command);
                });
    }

    /**
     * 設定定義を解析.
     *
     * @param context 機能出力モデルマップ
     */
    private void parseConfigurations(DevGeneratorContext context) {

        // Command
        Optional.ofNullable(context.getSpec().getConfigurations())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(configuration -> {

                    // Localeが関係ないものはここで処理する.
                    ConfigurationModel com = new ConfigurationModel();
                    com.setId(configuration.getId());

                    context.getFunctionModelMap().forEach((k, v) -> {
                        v.getConfigurations().put(com.getId(), SerializationUtils.clone(com));
                    });

                    // Summary
                    Optional.ofNullable(configuration.getSummary())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .forEach(x -> {
                                if (context.getFunctionModelMap().containsKey(x.getLang())) {
                                    context.getFunctionModelMap()
                                            .get(x.getLang())
                                            .getConfiguration(configuration.getId())
                                            .addSummary(x.getContents());
                                }
                            });

                    // Configuration
                    Optional.ofNullable(configuration.getDescription())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .forEach(x -> {
                                x.getSummary().forEach(y -> {
                                    if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                        context.getFunctionModelMap()
                                                .get(y.getLang())
                                                .getConfiguration(configuration.getId())
                                                .addDescription(y.getContents());
                                    }
                                });
                            });

                    // Structure Description
                    Optional.ofNullable(configuration.getStructure())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty)
                            .sorted(StructureComparator.getInstance())
                            .forEach(x -> {
                                if (CollectionUtils.isNotEmpty(x.getDescription())) {
                                    x.getDescription().forEach(y -> {
                                        if (context.getFunctionModelMap().containsKey(y.getLang())) {
                                            context.getFunctionModelMap()
                                                    .get(y.getLang())
                                                    .getConfiguration(configuration.getId())
                                                    .addStructureDescription(y.getContents());
                                        }
                                    });
                                }
                            });

                    // YAML構成を作成
                    createStructure(context, configuration);
                });
    }

    /**
     * YAML構成を作成.
     *
     * @param context コンテキスト
     * @param flow Flow
     */
    private void createStructure(DevGeneratorContext context, Flow flow) {
        for (Map.Entry<String, FunctionModel> entry : context.getFunctionModelMap().entrySet()) {
            StringBuilder sb = new StringBuilder();
            createStructureRecursive(entry.getKey(), sb, 0, flow.getStructure());
            entry.getValue().getFlow(flow.getId()).setStructure(sb.toString());
            System.out.println(sb.toString());
        }
    }

    /**
     * YAML構成を作成.
     *
     * @param context コンテキスト
     * @param command コマンド
     */
    private void createStructure(DevGeneratorContext context, Command command) {
        for (Map.Entry<String, FunctionModel> entry : context.getFunctionModelMap().entrySet()) {
            StringBuilder sb = new StringBuilder();
            createStructureRecursive(entry.getKey(), sb, 0, command.getStructure());
            entry.getValue().getCommand(command.getId()).setStructure(sb.toString());
            System.out.println(sb.toString());
        }
    }

    /**
     * YAML構成を作成.
     *
     * @param context コンテキスト
     * @param configuration コマンド
     */
    private void createStructure(DevGeneratorContext context, Configuration configuration) {
        for (Map.Entry<String, FunctionModel> entry : context.getFunctionModelMap().entrySet()) {
            StringBuilder sb = new StringBuilder();
            createStructureRecursive(entry.getKey(), sb, 0, configuration.getStructure());
            entry.getValue().getConfiguration(configuration.getId()).setStructure(sb.toString());
            System.out.println(sb.toString());
        }
    }

    /**
     * コマンドの構成を作成.
     *
     * @param locale
     * @param sb
     * @param level
     * @param structures
     */
    private void createStructureRecursive(String locale, StringBuilder sb, int level, List<Structure> structures) {

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
                // TODO : objectであるのに、配下のプロパティが存在しない場合にNullPointerExceptionになるため一時的に回避
                if (structure.getProperty() != null) {
                    createStructureRecursive(locale, sb, level + 1, structure.getProperty());
                }
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
