package org.smrp.services;

import org.bson.Document;
import org.smrp.Constants;
import org.smrp.models.CommonSetup;

import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;


@Singleton 
public class ReportService {
    
    private final CommonSetupService commonSetupService;
    
    public ReportService(CommonSetupService commonSetupService) {
        this.commonSetupService = commonSetupService;
    }

    public Mono<String> refReferralSourceCode(Document doc) {
        return getCode("REFERRAL", doc, "referral");
    }

    public Mono<String> refPersonTitleCode(Document doc) {
        return getCode("TITLE", doc, "title");
    }

    public Mono<String> refGenderCode(Document doc) {
        return getCode("GENDER", doc, "gender");
    }

    public Mono<String> refGenderCode1(Document doc) {
        return getCode("CHILD_SEX", doc, "gender");
    }

    public Mono<String> refMaritalStatusCode(Document doc) {
        return getCode("MARITAL_STATUS", doc, "marital_status");
    }

    public Mono<String> refReligionCode(Document doc) {
        return getCode("RELIGION", doc, "religion");
    }

    public Mono<String> refCitizenshipCode(Document doc) {
        return getCode("NATIONALITY", doc, "country");
    }

    public Mono<String> refCitizenshipCodeNOK(Document doc) {
        return getCode("NOK_NATIONALITY", doc, "country");
    }

    public Mono<String> refEthnicCode(Document doc) {
        return getCode("ETHNIC_GROUP", doc, "ethnic_group");
    }

    public Mono<String> refForeignerOriginCountryCode(Document doc) {
        return getCode("COUNTRY_OF_BIRTH", doc, "country");
    }

    public Mono<String> refForeignerResidenceCountryCode(Document doc) {
        return getCode("REFFOREIGNRCOUNTRYCODE", doc, "country");
    }

    public Mono<String> refPersonCategoryCode(Document doc) {
        return getCode("REFPERSONCATEGORYCODE", doc, "person_category_code");
    }

    public Mono<String> refIdentificationTypeCode(Document doc) {
        return getCode("DOCUMENT_TYPE", doc, "id_type");
    }

    public Mono<String> refCityCode(Document doc) {
        return getCode("CITYCODE", doc, "city");
    }

    public Mono<String> refCityCodeNOK(Document doc) {
        return getCode("NOK_CITYCODE", doc, "city");
    }

    public Mono<String> refStateCode(Document doc) {
        return getCode("OCITY", doc, "state");
    }

    public Mono<String> refStateCodeNOK(Document doc) {
        return getCode("NOK_OCITY", doc, "state");
    }

    public Mono<String> refPersonTitleCodeNOK(Document doc) {
        return getCode("NOK_TITLE", doc, "title");
    }

    public Mono<String> refRelationshipCode(Document doc) {
        return getCode("RELATION_DESCRIPTION", doc, "relationship");
    }

    public Mono<String> refIdentificationTypeCodeNOK(Document doc) {
        return getCode("NOK_ID_TYPE", doc, "id_type");
    }

    public Mono<String> refDisciplineCode(Document doc) {
        return getCode("PRIMARY_SPECIALITY", doc, "speciality");
    }

    public Mono<String> refDisciplineCode1(Document doc) {
        return getCode("PRIMARY_SPECIALTY", doc, "speciality");
    }

    public Mono<String> refWardClassCode(Document doc) {
        return getCode("PAYMENT_CLASS_CODE", doc, "ward_class");
    }

    public Mono<String> refDischargeTypeCode(Document doc) {
        return getCode("DISCHARGE_REASON", doc, "discharge_type");
    }

    public Mono<String> refDiagnosisItemTypeCode(Document doc) {
        return getCode("DIAGNOSIS_DESC", doc, "diag_item_type");
    }

    public Mono<String> refLabourModeCode(Document doc) {
        return getCode("DELIVERY_TYPE", doc, "delivery_type");
    }

    public Mono<String> getCode(String key, Document doc, String table) {
        final String x = Constants.NO_INFO;
        String s = get(key, doc);
        if ("".equals(s)) {
            return Mono.just(x);
        }
        return commonSetupService.findByDesc(s, table)
            .map(CommonSetup::getCode)
            .map(c -> (c == null || c.trim().isEmpty()) ? x : c)
            .defaultIfEmpty(x)
            .onErrorResume(e -> Mono.just(x));
    }

    public String get(String key, Document doc) {
        String s = "";
        if (doc == null) {
            return s;
        }
        Object v = doc.get(key);
        if (v != null) {
            s = String.valueOf(v);
            s = s.trim();
        }
        return s;
    }
}
