package fi.vm.yti.codelist.api.export;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import fi.vm.yti.codelist.api.domain.Domain;
import fi.vm.yti.codelist.common.dto.CodeDTO;
import fi.vm.yti.codelist.common.dto.CodeSchemeDTO;
import fi.vm.yti.codelist.common.dto.ExtensionSchemeDTO;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

@Component
public class CodeSchemeExporter extends BaseExporter {

    private Domain domain;
    private CodeExporter codeExporter;
    private ExtensionSchemeExporter extensionSchemeExporter;
    private ExtensionExporter extensionExporter;

    public CodeSchemeExporter(final Domain domain,
                              final CodeExporter codeExporter,
                              final ExtensionSchemeExporter extensionSchemeExporter,
                              final ExtensionExporter extensionExporter) {
        this.domain = domain;

        this.codeExporter = codeExporter;
        this.extensionSchemeExporter = extensionSchemeExporter;
        this.extensionExporter = extensionExporter;
    }

    public String createCsv(final Set<CodeSchemeDTO> codeSchemes) {
        final Set<String> prefLabelLanguages = resolveCodeSchemePrefLabelLanguages(codeSchemes);
        final Set<String> definitionLanguages = resolveCodeSchemeDefinitionLanguages(codeSchemes);
        final Set<String> descriptionLanguages = resolveCodeSchemeDescriptionLanguages(codeSchemes);
        final Set<String> changeNoteLanguages = resolveCodeSchemeChangeNoteLanguages(codeSchemes);
        final String csvSeparator = ",";
        final StringBuilder csv = new StringBuilder();
        appendValue(csv, csvSeparator, CONTENT_HEADER_CODEVALUE);
        appendValue(csv, csvSeparator, CONTENT_HEADER_ID);
        appendValue(csv, csvSeparator, CONTENT_HEADER_CLASSIFICATION);
        appendValue(csv, csvSeparator, CONTENT_HEADER_VERSION);
        appendValue(csv, csvSeparator, CONTENT_HEADER_STATUS);
        appendValue(csv, csvSeparator, CONTENT_HEADER_SOURCE);
        appendValue(csv, csvSeparator, CONTENT_HEADER_LEGALBASE);
        appendValue(csv, csvSeparator, CONTENT_HEADER_GOVERNANCEPOLICY);
        appendValue(csv, csvSeparator, CONTENT_HEADER_DEFAULTCODE);
        prefLabelLanguages.forEach(language -> appendValue(csv, csvSeparator, CONTENT_HEADER_PREFLABEL_PREFIX + language.toUpperCase()));
        definitionLanguages.forEach(language -> appendValue(csv, csvSeparator, CONTENT_HEADER_DEFINITION_PREFIX + language.toUpperCase()));
        descriptionLanguages.forEach(language -> appendValue(csv, csvSeparator, CONTENT_HEADER_DESCRIPTION_PREFIX + language.toUpperCase()));
        changeNoteLanguages.forEach(language -> appendValue(csv, csvSeparator, CONTENT_HEADER_CHANGENOTE_PREFIX + language.toUpperCase()));
        appendValue(csv, csvSeparator, CONTENT_HEADER_STARTDATE);
        appendValue(csv, csvSeparator, CONTENT_HEADER_ENDDATE);
        appendValue(csv, csvSeparator, CONTENT_HEADER_CREATED);
        appendValue(csv, csvSeparator, CONTENT_HEADER_MODIFIED, true);
        for (final CodeSchemeDTO codeScheme : codeSchemes) {
            appendValue(csv, csvSeparator, codeScheme.getCodeValue());
            appendValue(csv, csvSeparator, codeScheme.getId().toString());
            appendValue(csv, csvSeparator, formatDataClassificationsToString(codeScheme.getDataClassifications()));
            appendValue(csv, csvSeparator, codeScheme.getVersion());
            appendValue(csv, csvSeparator, codeScheme.getStatus());
            appendValue(csv, csvSeparator, codeScheme.getSource());
            appendValue(csv, csvSeparator, codeScheme.getLegalBase());
            appendValue(csv, csvSeparator, codeScheme.getGovernancePolicy());
            appendValue(csv, csvSeparator, codeScheme.getDefaultCode() != null ? codeScheme.getDefaultCode().getCodeValue() : "");
            prefLabelLanguages.forEach(language -> appendValue(csv, csvSeparator, codeScheme.getPrefLabel().get(language)));
            definitionLanguages.forEach(language -> appendValue(csv, csvSeparator, codeScheme.getDefinition().get(language)));
            descriptionLanguages.forEach(language -> appendValue(csv, csvSeparator, codeScheme.getDescription().get(language)));
            changeNoteLanguages.forEach(language -> appendValue(csv, csvSeparator, codeScheme.getChangeNote().get(language)));
            appendValue(csv, csvSeparator, codeScheme.getStartDate() != null ? formatDateWithISO8601(codeScheme.getStartDate()) : "");
            appendValue(csv, csvSeparator, codeScheme.getEndDate() != null ? formatDateWithISO8601(codeScheme.getEndDate()) : "");
            appendValue(csv, csvSeparator, codeScheme.getCreated() != null ? formatDateWithSeconds(codeScheme.getCreated()) : "");
            appendValue(csv, csvSeparator, codeScheme.getModified() != null ? formatDateWithSeconds(codeScheme.getModified()) : "", true);
        }
        return csv.toString();
    }

    public Workbook createExcel(final CodeSchemeDTO codeScheme,
                                final String format) {
        final Workbook workbook = createWorkBook(format);
        final Set<CodeSchemeDTO> codeSchemes = new HashSet<>();
        codeSchemes.add(codeScheme);
        addCodeSchemeSheet(workbook, EXCEL_SHEET_CODESCHEMES, codeSchemes);
        final String codeSheetName = truncateSheetName(EXCEL_SHEET_CODES + "_" + codeScheme.getCodeValue());
        codeExporter.addCodeSheet(workbook, codeSheetName, domain.getCodesByCodeRegistryCodeValueAndCodeSchemeCodeValue(codeScheme.getCodeRegistry().getCodeValue(), codeScheme.getCodeValue()));
        final Set<ExtensionSchemeDTO> extensionSchemes = domain.getExtensionSchemes(null, null, null, codeScheme, null, null);
        final String extensionSchemeSheetName = truncateSheetName(EXCEL_SHEET_EXTENSIONSCHEMES + "_" + codeScheme.getCodeValue());
        if (extensionSchemes != null && !extensionSchemes.isEmpty()) {
            extensionSchemeExporter.addExtensionSchemesSheet(workbook, extensionSchemeSheetName, extensionSchemes);
            int i = 0;
            for (final ExtensionSchemeDTO extensionScheme : extensionSchemes) {
                final String extensionSheetName = truncateSheetNameWithIndex(EXCEL_SHEET_EXTENSIONS + "_" + codeScheme.getCodeValue() + "_" + extensionScheme.getCodeValue(), ++i);
                extensionExporter.addExtensionsSheet(workbook, extensionSheetName, domain.getExtensions(null, null, extensionScheme, null, null));
            }
        } else {
            extensionSchemeExporter.addExtensionSchemesSheet(workbook, extensionSchemeSheetName, new HashSet<ExtensionSchemeDTO>());
        }
        return workbook;
    }

    public Workbook createExcel(final Set<CodeSchemeDTO> codeSchemes,
                                final String format) {
        final Workbook workbook = createWorkBook(format);
        addCodeSchemeSheet(workbook, EXCEL_SHEET_CODESCHEMES, codeSchemes);
        return workbook;
    }

    private void addCodeSchemeSheet(final Workbook workbook,
                                    final String sheetName,
                                    final Set<CodeSchemeDTO> codeSchemes) {
        final Set<String> prefLabelLanguages = resolveCodeSchemePrefLabelLanguages(codeSchemes);
        final Set<String> definitionLanguages = resolveCodeSchemeDefinitionLanguages(codeSchemes);
        final Set<String> descriptionLanguages = resolveCodeSchemeDescriptionLanguages(codeSchemes);
        final Set<String> changeNoteLanguages = resolveCodeSchemeChangeNoteLanguages(codeSchemes);
        final Sheet sheet = workbook.createSheet(sheetName);
        final Row rowhead = sheet.createRow((short) 0);
        int j = 0;
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_ID);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_CODEVALUE);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_CLASSIFICATION);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_VERSION);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_STATUS);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_SOURCE);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_LEGALBASE);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_GOVERNANCEPOLICY);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_DEFAULTCODE);
        for (final String language : prefLabelLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_PREFLABEL_PREFIX + language.toUpperCase());
        }
        for (final String language : definitionLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_DEFINITION_PREFIX + language.toUpperCase());
        }
        for (final String language : descriptionLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_DESCRIPTION_PREFIX + language.toUpperCase());
        }
        for (final String language : changeNoteLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_CHANGENOTE_PREFIX + language.toUpperCase());
        }
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_STARTDATE);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_ENDDATE);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_CREATED);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_MODIFIED);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_CODESSHEET);
        rowhead.createCell(j).setCellValue(CONTENT_HEADER_EXTENSIONSCHEMESSHEET);
        int i = 1;
        for (final CodeSchemeDTO codeScheme : codeSchemes) {
            final Row row = sheet.createRow(i++);
            int k = 0;
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getId().toString()));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getCodeValue()));
            row.createCell(k++).setCellValue(checkEmptyValue(formatDataClassificationsToString(codeScheme.getDataClassifications())));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getVersion()));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getStatus()));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getSource()));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getLegalBase()));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getGovernancePolicy()));
            row.createCell(k++).setCellValue(checkEmptyValue(codeScheme.getDefaultCode() != null ? codeScheme.getDefaultCode().getCodeValue() : ""));
            for (final String language : prefLabelLanguages) {
                row.createCell(k++).setCellValue(codeScheme.getPrefLabel().get(language));
            }
            for (final String language : definitionLanguages) {
                row.createCell(k++).setCellValue(codeScheme.getDefinition().get(language));
            }
            for (final String language : descriptionLanguages) {
                row.createCell(k++).setCellValue(codeScheme.getDescription().get(language));
            }
            for (final String language : changeNoteLanguages) {
                row.createCell(k++).setCellValue(codeScheme.getChangeNote().get(language));
            }
            row.createCell(k++).setCellValue(codeScheme.getStartDate() != null ? formatDateWithISO8601(codeScheme.getStartDate()) : "");
            row.createCell(k++).setCellValue(codeScheme.getEndDate() != null ? formatDateWithISO8601(codeScheme.getEndDate()) : "");
            row.createCell(k++).setCellValue(codeScheme.getCreated() != null ? formatDateWithSeconds(codeScheme.getCreated()) : "");
            row.createCell(k++).setCellValue(codeScheme.getModified() != null ? formatDateWithSeconds(codeScheme.getModified()) : "");
            row.createCell(k++).setCellValue(checkEmptyValue(createCodesSheetName(codeScheme)));
            row.createCell(k).setCellValue(checkEmptyValue(createExtensionSchemesSheetName(codeScheme)));
        }
    }

    private Set<String> resolveCodeSchemePrefLabelLanguages(final Set<CodeSchemeDTO> codeSchemes) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final CodeSchemeDTO codeScheme : codeSchemes) {
            final Map<String, String> prefLabel = codeScheme.getPrefLabel();
            languages.addAll(prefLabel.keySet());
        }
        return languages;
    }

    private Set<String> resolveCodeSchemeDefinitionLanguages(final Set<CodeSchemeDTO> codeSchemes) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final CodeSchemeDTO codeScheme : codeSchemes) {
            final Map<String, String> definition = codeScheme.getDefinition();
            languages.addAll(definition.keySet());
        }
        return languages;
    }

    private Set<String> resolveCodeSchemeDescriptionLanguages(final Set<CodeSchemeDTO> codeSchemes) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final CodeSchemeDTO codeScheme : codeSchemes) {
            final Map<String, String> description = codeScheme.getDescription();
            languages.addAll(description.keySet());
        }
        return languages;
    }

    private Set<String> resolveCodeSchemeChangeNoteLanguages(final Set<CodeSchemeDTO> codeSchemes) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final CodeSchemeDTO codeScheme : codeSchemes) {
            final Map<String, String> changeNote = codeScheme.getChangeNote();
            languages.addAll(changeNote.keySet());
        }
        return languages;
    }

    private String formatDataClassificationsToString(final Set<CodeDTO> classifications) {
        final StringBuilder csvClassifications = new StringBuilder();
        int i = 0;
        for (final CodeDTO code : classifications) {
            i++;
            csvClassifications.append(code.getCodeValue().trim());
            if (i < classifications.size()) {
                csvClassifications.append(";");
            }
            i++;
        }
        return csvClassifications.toString();
    }
}
