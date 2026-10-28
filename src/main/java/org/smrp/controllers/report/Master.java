package org.smrp.controllers.report;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.smrp.services.ReportService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class Master {
    
    private final ReportService reportService;

    public Master(ReportService reportService) {
        this.reportService = reportService;
    }

    protected Mono<Map<String, String>> getData(Document doc) {
        Mono<String> refPersonTitleCode = reportService.refPersonTitleCode(doc);
        Mono<String> refIdentificationTypeCode = reportService.refIdentificationTypeCode(doc);
        Mono<String> refCityCode = reportService.refCityCode(doc);
        Mono<String> refStateCode = reportService.refStateCode(doc);
        Mono<String> refCountryCode = reportService.refCitizenshipCode(doc);

        Mono<String> refPersonTitleCodeNOK = reportService.refPersonTitleCodeNOK(doc);
        Mono<String> refIdentificationTypeCodeNOK = reportService.refIdentificationTypeCodeNOK(doc);
        Mono<String> refCityCodeNOK = reportService.refCityCodeNOK(doc);
        Mono<String> refStateCodeNOK = reportService.refStateCodeNOK(doc);
        Mono<String> refCitizenshipCodeNOK = reportService.refCitizenshipCodeNOK(doc);

        Mono<String> refReferralSourceCode = reportService.refReferralSourceCode(doc);
        Mono<String> refGenderCode = reportService.refGenderCode(doc);
        Mono<String> refMaritalStatusCode = reportService.refMaritalStatusCode(doc);
        Mono<String> refReligionCode = reportService.refReligionCode(doc);
        Mono<String> refCitizenshipCode = reportService.refCitizenshipCode(doc);

        Mono<String> refEthnicCode = reportService.refEthnicCode(doc);
        Mono<String> refForeignerOriginCountryCode = reportService.refForeignerOriginCountryCode(doc);
        Mono<String> refForeignerResidenceCountryCode = reportService.refForeignerResidenceCountryCode(doc);
        Mono<String> refPersonCategoryCode = reportService.refPersonCategoryCode(doc);
        Mono<String> refRelationshipCode = reportService.refRelationshipCode(doc);

        Mono<String> refDisciplineCode = reportService.refDisciplineCode(doc);
        Mono<String> refDisciplineCode1 = reportService.refDisciplineCode1(doc);
        Mono<String> refWardClassCode = reportService.refWardClassCode(doc);

        List<Mono<String>> monos = List.of(
            refPersonTitleCode, refIdentificationTypeCode, refCityCode, refStateCode, refCountryCode,
            refPersonTitleCodeNOK, refIdentificationTypeCodeNOK, refCityCodeNOK, refStateCodeNOK, refCitizenshipCodeNOK,
            refReferralSourceCode, refGenderCode, refMaritalStatusCode, refReligionCode, refCitizenshipCode,
            refEthnicCode, refForeignerOriginCountryCode, refForeignerResidenceCountryCode, refPersonCategoryCode, refRelationshipCode,
            refDisciplineCode, refDisciplineCode1, refWardClassCode
        );

        Mono<List<String>> resultMono = Flux.fromIterable(monos)
            .concatMap(mono -> mono)   // preserves order; use flatMap for concurrency
            .doOnError(e -> System.out.println("Error getting code: " + e.getMessage()))
            .collectList();

        return resultMono
            .map(list -> {
                return Map.ofEntries(
                    Map.entry("refPersonTitleCode", list.get(0)),
                    Map.entry("refIdentificationTypeCode", list.get(1)),
                    Map.entry("refCityCode", list.get(2)),
                    Map.entry("refStateCode", list.get(3)),
                    Map.entry("refCountryCode", list.get(4)),

                    Map.entry("refPersonTitleCodeNOK", list.get(5)),
                    Map.entry("refIdentificationTypeCodeNOK", list.get(6)),
                    Map.entry("refCityCodeNOK", list.get(7)),
                    Map.entry("refStateCodeNOK", list.get(8)),
                    Map.entry("refCitizenshipCodeNOK", list.get(9)),

                    Map.entry("refReferralSourceCode", list.get(10)),
                    Map.entry("refGenderCode", list.get(11)),
                    Map.entry("refMaritalStatusCode", list.get(12)),
                    Map.entry("refReligionCode", list.get(13)),
                    Map.entry("refCitizenshipCode", list.get(14)),

                    Map.entry("refEthnicCode", list.get(15)),
                    Map.entry("refForeignerOriginCountryCode", list.get(16)),
                    Map.entry("refForeignerResidenceCountryCode", list.get(17)),
                    Map.entry("refPersonCategoryCode", list.get(18)),
                    Map.entry("refRelationshipCode", list.get(19)),

                    Map.entry("refDisciplineCode", list.get(20)),
                    Map.entry("refDisciplineCode1", list.get(21)),
                    Map.entry("refWardClassCode", list.get(22))
                );
            })
            .defaultIfEmpty(Map.of())
            .onErrorResume(e -> Mono.just(Map.of()));
    }
}
