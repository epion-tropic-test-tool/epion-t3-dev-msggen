package com.epion_t3.dev.tools.messages.generator.component;

import com.epion_t3.core.common.bean.spec.Command;
import com.epion_t3.core.common.bean.spec.Content;
import com.epion_t3.core.common.bean.spec.ET3Spec;
import com.epion_t3.core.common.bean.spec.Structure;
import com.epion_t3.dev.tools.messages.generator.bean.CommandOutputModel;
import com.epion_t3.dev.tools.messages.generator.bean.ExecuteOptions;
import com.epion_t3.dev.tools.messages.generator.bean.FunctionOutputModel;
import com.epion_t3.dev.tools.messages.generator.comparator.FunctionComparator;
import com.epion_t3.dev.tools.messages.generator.comparator.StructureComparator;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ドキュメント生成処理.
 *
 * @author takashno
 */
public class DocumentGenerateComponent {

    /**
     * シングルトンインスタンス.
     */
    private static DocumentGenerateComponent instance = new DocumentGenerateComponent();

    /**
     * プライベートコンストラクタ.
     */
    private DocumentGenerateComponent() {
        // Do Nothing...
    }

    /**
     * シングルトンインスタンス取得.
     *
     * @return シングルトンインスタンス
     */
    public static DocumentGenerateComponent getInstance() {
        return instance;
    }

    /**
     * 出力処理.
     *
     * @param spec
     * @param options
     */
    public void generate(ET3Spec spec, ExecuteOptions options) {

        // Locale毎の機能コマンド出力モデルマップ
        Map<String, FunctionOutputModel> functionOutputModelMap = new HashMap<>();

        // 対象のLocaleについてコマンド出力モデルを作成
        spec.getLanguages().stream().forEach(x -> {
            Locale locale = Locale.forLanguageTag(x);
            FunctionOutputModel fom = new FunctionOutputModel();
            fom.setLabelName(spec.getInfo().getLabelName());
            fom.setName(spec.getInfo().getName());
            fom.setCustomPackage(spec.getInfo().getCustomPackage());
            fom.setLocale(locale);
            functionOutputModelMap.put(x, fom);
        });

        spec.getInfo().getSummary().stream()
                .forEach(x -> {
                    if (functionOutputModelMap.containsKey(x.getLang())) {
                        functionOutputModelMap.get(x.getLang())
                                .addSummary(x.getContents());
                    }
                });
        spec.getInfo().getDescription().stream()
                .forEach(x -> {
                    if (functionOutputModelMap.containsKey(x.getLang())) {
                        functionOutputModelMap.get(x.getLang())
                                .addDescription(x.getContents());
                    }
                });


        // Command
        for (Command command : spec.getCommands()) {

            // Localeが関係ないものはここで処理する.
            CommandOutputModel com = new CommandOutputModel();
            com.setId(command.getId());
            // Command Kind
            com.setAssertCommand(command.getAssertCommand());
            com.setEvidenceCommand(command.getEvidenceCommand());

            functionOutputModelMap.forEach((k, v) -> {
                v.getCommands().put(com.getId(), SerializationUtils.clone(com));
            });

            // Summary
            command.getSummary().stream()
                    .forEach(x -> {
                        if (functionOutputModelMap.containsKey(x.getLang())) {
                            functionOutputModelMap.get(x.getLang())
                                    .getCommand(command.getId())
                                    .addSummary(x.getContents());
                        }
                    });

            // Functions
            command.getFunction().stream()
                    .sorted(FunctionComparator.getInstance())
                    .forEach(x -> {
                        x.getSummary().forEach(y -> {
                            if (functionOutputModelMap.containsKey(y.getLang())) {
                                functionOutputModelMap.get(y.getLang())
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
                                if (functionOutputModelMap.containsKey(y.getLang())) {
                                    functionOutputModelMap.get(y.getLang())
                                            .getCommand(command.getId())
                                            .addStructureDescription(y.getContents());
                                }
                            });
                        }
                    });

            StringBuilder sb = new StringBuilder();
            createCommandStructure(sb, 0, command.getStructure());
            System.out.println(sb.toString());
        }


        createFunction(spec, functionOutputModelMap);

    }

    /**
     * @param sb
     * @param level
     * @param structures
     */
    private void createCommandStructure(StringBuilder sb, int level, List<Structure> structures) {
        if (level == 0) {
            sb.append("commands : \n");
        }
        StringBuilder levelSb = new StringBuilder();
        for (int i = 0; i < level + 1; i++) {
            levelSb.append("  ");
        }
        final String levelSpace = levelSb.toString();
        for (Structure structure : structures) {
            sb.append(levelSpace);
            sb.append(structure.getName());
            sb.append(" : ");
            switch (structure.getType()) {
                case "string":
                    for (Content content : structure.getSummary()) {
                        sb.append("\"");
                        sb.append(content.getContents());
                        sb.append("\"\n");
                    }
                    break;
                case "number":
                    sb.append("values\n");
                    break;
                default:
            }
        }
    }

    private void createFunction(ET3Spec spec,
                                Map<String, FunctionOutputModel> functionOutputModelMap) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile("function.mustache");
        for (Map.Entry<String, FunctionOutputModel> entry :
                functionOutputModelMap.entrySet()) {
            try (StringWriter sw = new StringWriter()) {
                m.execute(sw, entry.getValue());
                sw.flush();
                System.out.println(sw.toString());
            } catch (IOException e) {

            }
        }
    }

    private String createConfiguration() {
        return null;
    }

    private String createFlow() {
        return null;
    }


}
