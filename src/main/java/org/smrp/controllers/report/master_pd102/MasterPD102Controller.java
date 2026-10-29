package org.smrp.controllers.report.master_pd102;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.smrp.models.Pager;
import org.smrp.services.ReportService;
import org.smrp.Utils;
import org.smrp.controllers.report.Master;

import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.validation.Validated;


@Tag(name = "Report/MasterPD102")
@Secured(SecurityRule.IS_AUTHENTICATED)
@SecurityRequirement(name = "BearerAuth")
@Controller("/api/master-pd102")
@Validated
public class MasterPD102Controller extends Master {

    private final MongoClient mongoClient;
    private final String mongodbPrefix;
    private final String facilityCode;

    public MasterPD102Controller(
        MongoClient mongoClient,
        @Value("${mongodb.prefix}") String mongodbPrefix,
        @Value("${facilityCode}") String facilityCode,
        ReportService reportService
    ) {
        super(reportService);
        this.mongoClient = mongoClient;
        this.mongodbPrefix = mongodbPrefix;
        this.facilityCode = facilityCode;
    }
    
    @Get("/export/rpt1")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> jsonpd101(
        @QueryValue(value = "datefrom", defaultValue = "2023-01-01") String datefrom,
        @QueryValue(value = "dateto", defaultValue = "2024-01-01") String dateto,
        Authentication authentication
    ) {
        String username = authentication.getName();
        MongoCollection<Document> col = getCollection(username);

        String[] dt1 = datefrom.split("-");
        String[] dt2 = dateto.split("-");
        String ds1 = String.format("%s%s%s", dt1[2], dt1[1], dt1[0]);
        String ds2 = String.format("%s%s%s", dt2[2], dt2[1], dt2[0]);
        String filename = String.format("%s_%s_%s_PD102.json", facilityCode, ds1, ds2);

        return Flux.from(col.find())
            .collectList()
            .flatMap(ls -> {
                if (ls.isEmpty()) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("filename", filename);
                    data.put("birthDateFrom", datefrom);
                    data.put("birthDateTo", dateto);
                    data.put("refServiceTypeCode", "01");
                    data.put("facilityCode", facilityCode);
                    data.put("forms", List.of());
                    return Mono.just(send(data, filename));
                }
                
                List<Document> lr = Utils.processDoc(ls);
                List<Mono<Map<String, String>>> monos = lr.stream()
                        .map(this::getData)
                        .collect(Collectors.toList());

                return Mono.zip(monos, objects -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> lm = (List<Map<String, String>>) (List<?>) Arrays.asList(objects);
                    List<Map<String, Object>> forms = new ArrayList<>();
                    for (int i = 0; i < lr.size(); i++) {
                        Document doc = lr.get(i);
                        Map<String, String> md = lm.get(i);
                        Map<String, Object> person = new LinkedHashMap<>();
                        person.put("refPersonTitleCode", md.get("refPersonTitleCode"));
                        person.put("fullName", Utils.getStr(doc.get("PATIENT_NAME")));
                        person.put("refIdentificationTypeCode", md.get("refIdentificationTypeCode"));
                        person.put("identificationNo", Utils.getStr(doc.get("DOCUMENT_NUMBER")));
                        person.put("refAddressTypeCode", "C");
                        person.put("street1", Utils.getStr(doc.get("STREET1")));
                        person.put("street2", Utils.getStr(doc.get("STREET2")));
                        person.put("refCityCode", md.get("refCityCode"));
                        person.put("refPostCode", Utils.getStr(doc.get("POSTCODE")));
                        person.put("refStateCode", md.get("refStateCode"));
                        person.put("refCountryCode", md.get("refCitizenshipCode"));
                        person.put("refContactTypeCode", "02");
                        person.put("contactInfo", Utils.getStr(doc.get("HOME_PHONE")));
                        person.put("height", Utils.getNum(Utils.getStr(doc.get("PERSON_HEIGHT"))));
                        person.put("weight", Utils.getNum(Utils.getStr(doc.get("PERSON_WEIGHT"))));

                        Map<String, Object> nok = new LinkedHashMap<>();
                        nok.put("refPersonTitleCode", md.get("refPersonTitleCodeNOK"));
                        nok.put("fullName", Utils.getStr(doc.get("PATIENT_NOK_NAME")));
                        nok.put("refIdentificationTypeCode", md.get("refIdentificationTypeCodeNOK"));
                        nok.put("identificationNo", Utils.getStr(doc.get("NOK_ID")));
                        nok.put("refAddressTypeCode", "C");
                        nok.put("street1", Utils.getStr(doc.get("NOK_STREET1")));
                        nok.put("street2", Utils.getStr(doc.get("NOK_STREET2")));
                        nok.put("refCityCode", md.get("refCityCodeNOK"));
                        nok.put("refPostCode", Utils.getStr(doc.get("NOK_POSTCODE")));
                        nok.put("refStateCode", md.get("refStateCodeNOK"));
                        nok.put("refCountryCode", md.get("refCitizenshipCodeNOK"));
                        nok.put("refContactTypeCode", "02");
                        nok.put("contactInfo", Utils.getStr(doc.get("NOK_MOBILE_PHONE")));

                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("rn", Utils.getStr(doc.get("ACCOUNT_NO")));
                        m.put("mrn", Utils.getStr(doc.get("PRN")));
                        m.put("eventDate", String.format("%s %s:00", 
                                doc.get("REGISTRATION_DATE"),
                                doc.get("REGISTRATION_TIME")));
                        m.put("isPoliceCase", "02");
                        m.put("internalReferral", "false");
                        m.put("refReferralSourceCode", md.get("refReferralSourceCode"));
                        m.put("refGenderCode", md.get("refGenderCode"));
                        m.put("dob", Utils.getStr(doc.get("DOB")));
                        m.put("refMaritalStatusCode", md.get("refMaritalStatusCode"));
                        m.put("refReligionCode", md.get("refReligionCode"));
                        m.put("refCitizenshipCode", md.get("refCitizenshipCode"));
                        m.put("refEthnicCode", md.get("refEthnicCode"));
                        m.put("refForeignerOriginCountryCode", md.get("refForeignerOriginCountryCode"));
                        m.put("refForeignerResidenceCountryCode", md.get("refForeignerResidenceCountryCode"));
                        m.put("refPersonCategoryCode", md.get("refPersonCategoryCode"));
                        m.put("refRelationshipCode", md.get("refRelationshipCode"));
                        m.put("refWardTransitionTypeCode", "A");
                        m.put("wardDateTime", String.format("%s %s:00", 
                                doc.get("ADMISSION_DATE"),
                                doc.get("ADMISSION_TIME")));
                        m.put("wardCode", Utils.getStr(doc.get("WARD_NO")));
                        m.put("refDisciplineCode", md.get("refDisciplineCode1"));
                        m.put("refSpecialityCode", md.get("refDisciplineCode1"));
                        m.put("refSubSpecialityCode", md.get("refDisciplineCode1"));
                        m.put("refWardClassCode", md.get("refWardClassCode"));
                        m.put("refWardCategoryCode", "00");
                        m.put("gravida", Utils.getStr(doc.get("GRAVIDA")));
                        m.put("para", Utils.getStr(doc.get("PARITY")));
                        m.put("periodOfGestationDay", Utils.getNumber(Utils.getStr(doc.get("GESTATION_PERIOD"))) * 7);
                        m.put("periodOfGestationWeek", Utils.getStr(doc.get("GESTATION_PERIOD")));
                        m.put("isMotherAlive", doc.get("ISMOTHERALIVE"));
                        m.put("refAntenatalCareCode", Utils.getStr(doc.get("REFANTENATALCARECODE")));
                        m.put("refLabourStatusCode", Utils.getStr(doc.get("LABOUR_METHOD")));
                        m.put("labourDateTime", String.format("%s 00:00:00", doc.get("DELIVERY_DATE")));
                        m.put("bornBeforeArrival", "N/A");
                        m.put("isBabyAlive", Utils.getStr(doc.get("RESULT_OF_BIRTH")));
                        m.put("refLabourTypeCode", "N/A");
                        m.put("refLabourModeCode", md.get("refLabourModeCode"));
                        m.put("refGenderCode1", md.get("refGenderCode1"));
                        m.put("birthWeight", Utils.getNum(Utils.getStr(doc.get("WEIGHT"))));
                        m.put("birthLength", Utils.getNum(Utils.getStr(doc.get("LENGTH"))));
                        m.put("birthHeadCircumference", "N/A");
                        m.put("refBloodTypeCode", "N/A");
                        m.put("refRhesusCode", "N/A");
                        m.put("refMin1ApgarScoreCode", "N/A");
                        m.put("refMin5ApgarScoreCode", "N/A");
                        m.put("refLabourComplicationCodes", "N/A");
                        m.put("person", person);
                        m.put("nextOfKins", nok);

                        forms.add(m);
                    }
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("filename", filename);
                    data.put("birthDateFrom", datefrom);
                    data.put("birthDateTo", dateto);
                    data.put("refServiceTypeCode", "01");
                    data.put("facilityCode", facilityCode);
                    data.put("forms", forms);
                    return send(data, filename);
                });
            });
    }

    @Get("/export/rpt1/xlsx")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @ExecuteOn(TaskExecutors.IO)
    public Mono<HttpResponse<?>> xlsx(
        @QueryValue(value = "datefrom", defaultValue = "2023-01-01") String datefrom,
        @QueryValue(value = "dateto", defaultValue = "2024-01-01") String dateto,
        Authentication authentication
    ) {
        String username = authentication.getName();
        MongoCollection<Document> col = getCollection(username);

        String[] dt1 = datefrom.split("-");
        String[] dt2 = dateto.split("-");
        String ds1 = String.format("%s%s%s", dt1[2], dt1[1], dt1[0]);
        String ds2 = String.format("%s%s%s", dt2[2], dt2[1], dt2[0]);
        String pf = "PD102";
        String filename = String.format("%s_%s_%s_%s.xlsx", facilityCode, ds1, ds2, pf);

        return Flux.from(col.find())
            .collectList()
            .flatMap(ls -> {
                try {
                    List<Document> lr = Utils.processDoc(ls);
                    byte[] bx = Utils.getXlsx(ColMap.COLUMN_MAP, lr);
                    return Mono.just(send(bx, filename));
                } catch (Exception e) {
                    return Mono.error(new RuntimeException(e));
                }
            });
    }

    @Get("/rpt1")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> list(
        @QueryValue(value = "_page", defaultValue = "1") Integer page,
        @QueryValue(value = "_limit", defaultValue = "10") Integer limit,
        @QueryValue(value = "datefrom", defaultValue = "") String datefrom,
        @QueryValue(value = "dateto", defaultValue = "") String dateto,
        Authentication authentication
    ) {
        String username = authentication.getName();
        MongoDatabase db = getDb();
        MongoCollection<Document> col = db.getCollection(String.format("__%s__", username));
        MongoCollection<Document> col2 = db.getCollection(String.format("__%s-q__", username));
        return Mono.from(col.countDocuments())
            .flatMap(total -> {
                return Flux.from(col2.find())
                    .collectList()
                    .flatMap(ld -> {
                        final String dateFrom = !ld.isEmpty() ? ld.get(0).getString("datefrom") : datefrom;
                        final String dateTo = !ld.isEmpty() ? ld.get(0).getString("dateto") : dateto;
                        
                        Pager pg = Pager.from(total, page, limit);
                        return Flux.from(col.find().skip(pg.getLowerBound()).limit(pg.getPageSize()))
                            .collectList()
                            .flatMap(ls -> {
                                List<Document> lr = Utils.processDoc(ls);
                                Map<String, Object> result = new LinkedHashMap<>();
                                result.put("columnmaps", ColMap.COLUMN_MAP);
                                result.put("total_count", total);
                                result.put("total_page", pg.getTotalPages());
                                result.put("page", pg.getPageNum());
                                result.put("data", lr);
                                result.put("datefrom", dateFrom);
                                result.put("dateto", dateTo);
                                return Mono.just(HttpResponse.ok(result));
                            });
                    });
            });
    }

    @Get("/rpt1/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> edit(
        String id,
        Authentication authentication
    ) {
        String username = authentication.getName();
        MongoCollection<Document> col = getCollection(username);
        try {
            ObjectId objectId = new ObjectId(id);
            Bson filter = Filters.eq("_id", objectId);
            return Mono.from(col.find(filter).first())
                .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Record not found")))
                .flatMap(doc -> {
                    doc.put("_id", doc.getObjectId("_id").toString());
                    return Mono.just(HttpResponse.ok(doc));
                });
        } catch (Exception e) {
            return Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        }
    }

    @Put("/rpt1/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> update(
        String id,
        @Body Map<String, Object> data,
        Authentication authentication
    ) {
        String username = authentication.getName();
        MongoCollection<Document> col = getCollection(username);
        ObjectId objectId = new ObjectId(id);
        Bson filter = Filters.eq("_id", objectId);
        data.put("_id", objectId);
        Document updateData = new Document("$set", data);
        HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
        return Mono.from(col.findOneAndUpdate(filter, updateData))
            .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Record not found")))
            .flatMap(doc -> Mono.just(response));
    }

    private MongoCollection<Document> getCollection(String username) {
        String s = String.format("__%s__", username);
        MongoDatabase db = getDb();
        return db.getCollection(s);
    }

    private MongoDatabase getDb() {
        String suffix = "prod".equals(mongodbPrefix) ? "_prod" : "";
        String s = String.format("master_pd102%s", suffix);
        return mongoClient.getDatabase(s);
    }
}
