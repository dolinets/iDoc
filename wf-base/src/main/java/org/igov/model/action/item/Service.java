package org.igov.model.action.item;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
//import static org.igov.io.fs.FileSystemData;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static org.igov.io.fs.FileSystemData.getSmartPathFileContent_ActionItem;

/**
 * User: goodg_000
 * Date: 04.05.2015
 * Time: 23:10
 */
@javax.persistence.Entity
@ApiModel(description="Зарегистрированные сервисы (услуги)")
public class Service extends org.igov.model.core.NamedEntity {

    @JsonProperty(value = "nOrder")
    @Column(name = "nOrder", nullable = false)
    @ApiModelProperty(value = "Целочисленный коэффициент, определяющий важность услуги, ее очередность в показе пользователю. По умолчанию ставим 100. Чем меньше, тем выше по списку стоит услуга. У услуг с одинаковым номером сортируется по алфавиту", required = true)
    private Integer order;

    @JsonProperty(value = "oSubcategory")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Subcategory", nullable = false)
    @ApiModelProperty(value = "Идентификатор номер-ИД подкатегории (Subcategory.csv) (1-Будівництво, нерухомість, земля, 2-Взаємодія з державними органами, 3- Поліція и т.д.). Особое внимание необходимо уделить nID_Category в подкатегории, так как разделяют подкатегории для Громадян (nID_Category=1) и Бизнес (nID_Category=2), т.е. одна и та же по названию подкатегория представлена двумя записями (для граждан и юрлиц)", required = true)
    private Subcategory subcategory;
    
    @JsonProperty(value = "aServiceData")
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ServiceData> serviceDataList = new ArrayList<>();

    @JsonProperty(value = "sInfo")
    @Column(name = "sInfo", nullable = false)
    @ApiModelProperty(value = "Признак наличия информации об услуге \"Як це працює\". Может быть пустым. Чтобы добавить информацию \"Як це працює\" (описание) по услуге необходимо заполнить атрибут значением [*] и создать HTML-страницу в wf-central/src/main/resources/patterns/services/info/nID.html, где nID - Уникальный номер-ИД услуги из файла Service.csv", required = false)
    private String info;

    @Column(name = "sFAQ", nullable = false)
    @JsonProperty("sFAQ")
    @ApiModelProperty(value = "Признак наличия информации о часто задаваемых вопросах (FAQ) по услуге. Может быть пустым. Чтобы добавить FAQ по услуге необходимо заполнить атрибут значением [*] и создать HTML-страницу в wf-central/src/main/resources/patterns/services/FAQ/nID.html, где nID - Уникальный номер-ИД услуги из файла Service.csv", required = false)
    private String faq;

    @Column(name = "sLaw", nullable = false)
    @JsonProperty("sLaw")
    @ApiModelProperty(value = "Признак наличия ссылок на законы, инструкции по услуге. Может быть пустым. Чтобы добавить информацию о ссылках на законы, инструкции по услуге необходимо заполнить атрибут значением [*] и создать HTML-страницу в wf-central/src/main/resources/patterns/services/Law/nID.html, где nID - Уникальный номер-ИД услуги из файла Service.csv", required = false)
    private String law;

    @Column(name = "nOpenedLimit", nullable = false)
    @JsonProperty("nOpenedLimit")
    @ApiModelProperty(value = "Число максимального количества одновременно поданных (открытых) заявок от одного гражданина. По умолчанию ставим 0. Если атрибут равен 0, то ограничения на количество поданных заявок нет", required = true)
    private Integer nOpenedLimit;
    
    
    @JsonProperty(value = "sSubjectOperatorName")
    @Column(name = "sSubjectOperatorName", nullable = false)
    @ApiModelProperty(value = "Название органа, отвечающего за услугу (Міністерство внутрішніх справ, Міська рада и т.д.)", required = true)
    private String sSubjectOperatorName;

    @JsonProperty(value = "saKeyword")
    @Column(name = "saKeyword")
    @ApiModelProperty(value = "Необязательный перечень ключевых слов, разделенных запятой", required = false)
    private String saKeyword;

    @Transient
    private int sub = 0;

    @Transient
    private int nStatus = 0;

    public String getSubjectOperatorName() {
        return sSubjectOperatorName;
    }

    public void setSubjectOperatorName(String s) {
        this.sSubjectOperatorName = s;
    }

    @JsonProperty(value = "nSub")
    public int getSub() {
        return sub;
    }

    public void setSub(int n) {
        sub = n;
    }

    @JsonProperty(value = "nStatus")
    public int getStatus() {
        return nStatus;
    }

    public void setStatus(int n) {
        nStatus = n;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    @JsonGetter("aServiceData")
    public List<ServiceData> getServiceDataFiltered(boolean withTestServices) {
        if (serviceDataList == null) {
            return null;
        }

        List<ServiceData> res = new ArrayList<>();
        for (ServiceData oServiceData : serviceDataList) {
            if (!oServiceData.isHidden() && (withTestServices || !oServiceData.isTest())) {
                res.add(oServiceData);
            }
        }
        return res;
    }

    //0 - none
    //1 - test
    //2 - prod
    @JsonGetter("nID_Status")
    public int getStatusID() {
        return calcStatusID(serviceDataList, getSub());
    }

    public static int calcStatusID(List<ServiceData> serviceDataList, int sub) {
        if (serviceDataList == null) {
            return 0;
        }

        if (sub == 0) {
            for (ServiceData oServiceData : serviceDataList) {
                if (oServiceData.isTest() && !oServiceData.isHidden()) {
                    return 1;
                }
            }
            return 0;
        } else {
            for (ServiceData oServiceData : serviceDataList) {
                if (!oServiceData.isTest() && !oServiceData.isHidden()) {
                    return 2;
                }
            }
            return 1;
        }
    }

    @JsonSetter("nID_Status")
    public void setStatusID(int id) {
        // need to avoid exception in tests.
    }

    @JsonIgnore
    public List<ServiceData> getServiceDataList() {
        return serviceDataList;
    }

    public void setServiceDataList(List<ServiceData> serviceDataList) {
        this.serviceDataList = serviceDataList;
    }

    public String getInfo() {
        return getSmartPathFileContent_ActionItem(info, "Info", getId());
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getFaq() {
        return getSmartPathFileContent_ActionItem(faq, "FAQ", getId());
    }

    public void setFaq(String faq) {
        this.faq = faq;
    }

    public String getLaw() {
        return getSmartPathFileContent_ActionItem(law, "Law", getId());
    }

    public void setLaw(String law) {
        this.law = law;
    }

    public Integer getOpenedLimit() {
        return nOpenedLimit;
    }

    public void setOpenedLimit(Integer nOpenedLimit) {
        this.nOpenedLimit = nOpenedLimit;
    }

    public String getSaKeyword() {
        return saKeyword;
    }

    public void setSaKeyword(String saKeyword) {
        this.saKeyword = saKeyword;
    }
}
