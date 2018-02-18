/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.subject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import javassist.NotFoundException;
import org.igov.model.core.GenericEntityDao;
import org.igov.model.subject.*;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.service.business.subject.criteria.HierarchyCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сервис получения полной организационной иерархии (родитель - ребенок)
 *
 * @author inna
 */
@Component("subjectGroupTreeService")
@Service
public class SubjectGroupTreeService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubjectGroupTreeService.class);

    public static final String ORGAN = "Organ";
    public static final String HUMAN = "Human";
    public static final String ALL = "All";

    /**
     * флаг определяющий, что на вход был конкрентный тип ORGAN или HUMAN
     */
    private static boolean isSubjectType = false;
    private static final long FAKE_ROOT_SUBJECT_ID = 0;

    @Autowired
    private SubjectHumanDao oSubjectHumanDao;

    @Autowired
    private SubjectOrganDao oSubjectOrganDao;

    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    
    
    @Autowired
    @Qualifier("subjectGroupDao")
    private GenericEntityDao<Long, SubjectGroup> subjectGroupDao;

    @Autowired
    private SubjectGroupTreeDao oSubjectGroupTreeDao;

    @Autowired
    private SubjectGroupService oSubjectGroupService;
    
    @Autowired
    private SubjectHumanService oSubjectHumanService;
    
    @Autowired
    private SubjectOrganService oSubjectOrganService;


    //Мапа для укладывания ид родителя и его детей в методе получения иерархии  getChildrenTree
    Map<Long, List<SubjectGroup>> getChildrenTreeRes = new HashMap<>();

    public SubjectGroupResultTree getCatalogSubjectGroupsTree(String sID_Group_Activiti,
            Long deepLevel, String sFind, Boolean bIncludeRoot, Long deepLevelWidth,
            String sSubjectType){
        long startTime_0 = System.currentTimeMillis();
        //SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        //sID_Group_Activiti.equalsIgnoreCase(oSubjectGroup.getsChain());
        if (sID_Group_Activiti != null && sID_Group_Activiti.contains(",") && HUMAN.equals(sSubjectType) && sFind != null && !"".equals(sFind.trim())) {
            return oSubjectGroupService.getaSubjectGroup(sID_Group_Activiti, sFind);
        }
        List<SubjectGroup> resultTree = new ArrayList<>();
        SubjectGroupResultTree processSubjectResultTree = new SubjectGroupResultTree();
        //get all SubjectGroupTree

        //List<SubjectGroupTree> subjectGroupRelations = new ArrayList<>(oSubjectGroupTreeDao.findAll());
        //LOG.info("subjectGroupRelations.size: " + subjectGroupRelations.size());

        //в случае передачи сразу нескольких групп
        Set<String> asID_Group_Activiti = new HashSet(Arrays.asList(sID_Group_Activiti.split(",")));

        long stopTime_1 = System.currentTimeMillis();
        long elapsedTime_1 = stopTime_1 - startTime_0;
        LOG.info("find subjectGroupRelations time  {}", elapsedTime_1);



        for (String sID_Group_Activiti_Split : asID_Group_Activiti) {
            getChildrenTreeRes = new HashMap<>();
            /**
             * Лист для ид Subject ORGAN или HUMAN для последующего анализа
             */
            SubjectGroup oSubjectGroupCompany = null;
            List<SubjectHuman> subjectHumans = null;
            List<SubjectOrgan> subjectOrgans = null;

            try {
                oSubjectGroupCompany = getCompany(sID_Group_Activiti_Split);
                LOG.info("oSubjectGroupCompany: {}", oSubjectGroupCompany);
            } catch (NotFoundException ex) {
                LOG.info("NotFoundException with getCompany: " + ex.getMessage());
            }

            List<SubjectGroupTree> subjectGroupRelations = new ArrayList<>();

            if(oSubjectGroupCompany != null){
                subjectGroupRelations.addAll(oSubjectGroupTreeDao.getaSubjectGroupTreeByCompany(oSubjectGroupCompany.getsChain()));
                LOG.info("subjectGroupRelations.size new: " + subjectGroupRelations.size());
            }

            long startTime_2 = System.currentTimeMillis();

            List<Long> resSubjectTypeList = new ArrayList<>();
            List<SubjectGroup> aChildResult = new ArrayList<>();
            List<SubjectGroup> resultSubjectGroupTree = new ArrayList<>();
            List<SubjectGroupTree> subjectGroupRelationsByGroup = new ArrayList<>();

            if (!subjectGroupRelations.isEmpty()) {
                List<VSubjectGroupParentNode> parentSubjectGroups = new ArrayList<>();
                Map<Long, List<SubjectGroup>> subjToNodeMap = new HashMap<>();
                Map<SubjectGroup, List<SubjectGroup>> parentChildren = new HashMap<>();
                Map<String, Long> mapGroupActiviti = new HashMap<>();
                VSubjectGroupParentNode parentSubjectGroup = null;
                Set<Long> idParentList = new LinkedHashSet<>();

                if (null == sSubjectType) {
                    throw new RuntimeException("There is no such Subject type! Wrong sSubjectType=" + sSubjectType);
                } else //get all SubjectHuman
                switch (sSubjectType) {
                    case HUMAN:
                        long startTime_3 = System.currentTimeMillis();
                        if (subjectHumans == null || subjectHumans.isEmpty()) {
                            //subjectHumans = new ArrayList<>(oSubjectHumanDao.findAll());
                            if(oSubjectGroupCompany != null){
                                subjectHumans = new ArrayList<>(oSubjectHumanDao.getSubjectHumansBysChain(oSubjectGroupCompany.getsChain()));
                            }
                            LOG.info("subjectHumans size is {}", subjectHumans.size());
                        }   long stopTime_3 = System.currentTimeMillis();
                        long elapsedTime_3 = stopTime_3 - startTime_3;
                        LOG.info("find subjectHumans time  {}", elapsedTime_3);
                        LOG.info("subjectHumans.size: " + subjectHumans.size());
                        isSubjectType = true;
                        long startTime_4 = System.currentTimeMillis();
                        if (!subjectHumans.isEmpty()) {
                            List<Long> subjectHumansIdSubj = Lists
                                    .newArrayList(Collections2.transform(subjectHumans, new Function<SubjectHuman, Long>() {
                                        @Override
                                        public Long apply(SubjectHuman subjectHuman) {
                                            return subjectHuman.getoSubject().getId();
                                        }
                                    }));
                            LOG.info("subjectHumansIdSubj.size: " + subjectHumansIdSubj.size()
                                    + " subjectGroupRelations.size: " + subjectGroupRelations.size()
                                    + " sID_Group_Activiti_Split: " + sID_Group_Activiti_Split + "!");
                            
                            subjectGroupRelationsByGroup = Lists
                                    .newArrayList(Collections2.filter(subjectGroupRelations, new Predicate<SubjectGroupTree>() {
                                        @Override
                                        public boolean apply(SubjectGroupTree subjectGroupTree) {
                                            // получить только отфильтрованный
                                            // список по Humans
                                            return Objects.nonNull(subjectGroupTree.getoSubjectGroup_Parent().getoSubject())
                                                    && Objects.nonNull(subjectGroupTree.getoSubjectGroup_Child().getoSubject())
                                                    && (subjectGroupTree.getoSubjectGroup_Parent().getsID_Group_Activiti().equals(sID_Group_Activiti_Split)
                                                    || subjectHumansIdSubj.contains(subjectGroupTree.getoSubjectGroup_Parent().getoSubject().getId()))
                                                    && subjectHumansIdSubj.contains(subjectGroupTree.getoSubjectGroup_Child().getoSubject().getId());
                                        }
                                    }));
                            LOG.info("subjectGroupRelationsByGroup.size: " + subjectGroupRelationsByGroup.size()
                                    + " sID_Group_Activiti_Split: " + sID_Group_Activiti_Split + "!");
                            
                            resSubjectTypeList.addAll(subjectHumansIdSubj);
                            LOG.info("  " + resSubjectTypeList.size());
                        }   long stopTime_4 = System.currentTimeMillis();
                        long elapsedTime_4 = stopTime_4 - startTime_4;
                        LOG.info("process subjectHumans time  {}", elapsedTime_4);
                        //get all SubjectOrgan
                        break;
                    case ORGAN:
                        if (subjectOrgans == null || subjectOrgans.isEmpty()) {
                            //subjectOrgans = new ArrayList<>(oSubjectOrganDao.findAll());getSubjectHumansBysChain
                            if(oSubjectGroupCompany != null){
                                subjectOrgans = new ArrayList<>(oSubjectOrganDao.getSubjectOrgansBysChain(oSubjectGroupCompany.getsChain()));
                                LOG.info("subjectOrgans size is {}", subjectOrgans.size());
                            }
                        }   LOG.info("subjectOrgans.size: " + subjectOrgans.size());
                        isSubjectType = true;
                        if (!subjectOrgans.isEmpty()) {
                            List<Long> subjectOrgansIdSubj = Lists
                                    .newArrayList(Collections2.transform(subjectOrgans, new Function<SubjectOrgan, Long>() {
                                        @Override
                                        public Long apply(SubjectOrgan subjectOrgan) {
                                            return subjectOrgan.getoSubject().getId();
                                        }
                                    }));
                            LOG.info("subjectOrgansIdSubj.size: " + subjectOrgansIdSubj.size());
                            
                            subjectGroupRelationsByGroup = Lists
                                    .newArrayList(Collections2.filter(subjectGroupRelations, new Predicate<SubjectGroupTree>() {
                                        @Override
                                        public boolean apply(SubjectGroupTree subjectGroupTree) {
                                            // получить только отфильтрованный
                                            // список по Organs
                                            return Objects.nonNull(subjectGroupTree.getoSubjectGroup_Parent().getoSubject())
                                                    && Objects.nonNull(subjectGroupTree.getoSubjectGroup_Child().getoSubject())
                                                    && (subjectGroupTree.getoSubjectGroup_Parent().getsID_Group_Activiti().equals(sID_Group_Activiti_Split)
                                                    || subjectOrgansIdSubj.contains(subjectGroupTree.getoSubjectGroup_Parent().getoSubject().getId()))
                                                    && subjectOrgansIdSubj.contains(subjectGroupTree.getoSubjectGroup_Child().getoSubject().getId());
                                        }
                                    }));
                            LOG.info("subjectGroupRelationsByGroup.size: " + subjectGroupRelationsByGroup.size());
                            
                            resSubjectTypeList.addAll(subjectOrgansIdSubj);
                            LOG.info("subjectGroupRelationsByGroup.size ORGAN: " + subjectGroupRelationsByGroup.size());
                        }   break;
                    default:
                        throw new RuntimeException("There is no such Subject type! Wrong sSubjectType=" + sSubjectType);
                }

                long startTime_5 = System.currentTimeMillis();

                for (SubjectGroupTree subjectGroupRelation : subjectGroupRelationsByGroup) {

                    SubjectGroup parent = subjectGroupRelation.getoSubjectGroup_Parent();

                    if (parent.getId() != FAKE_ROOT_SUBJECT_ID) {
                        parentSubjectGroup = new VSubjectGroupParentNode();
                        final SubjectGroup child = subjectGroupRelation.getoSubjectGroup_Child();
                        LOG.debug("SubjectGroup parent " + parent.getsID_Group_Activiti()
                                + " child: " + subjectGroupRelation.getoSubjectGroup_Child());
                        if (!idParentList.contains(parent.getId())) {
                            idParentList.add(parent.getId());
                            // устанавливаем парентов
                            parentSubjectGroup.setGroup(parent);
                            // доавляем детей
                            parentSubjectGroup.addChild(child);
                            parentSubjectGroups.add(parentSubjectGroup);
                            // мапа парент -ребенок
                            subjToNodeMap.put(parent.getId(), parentSubjectGroup.getChildren());
                            parentChildren.put(parent, parentSubjectGroup.getChildren());
                            // мапа группа-ид парента
                            mapGroupActiviti.put(parent.getsID_Group_Activiti(), parent.getId());
                            LOG.debug("!add mapGroupActiviti: " + parent.getsID_Group_Activiti()
                                    + "nID parent: " + parent.getId());
                        } else {
                            for (VSubjectGroupParentNode vSubjectGroupParentNode : parentSubjectGroups) {
                                // убираем дубликаты
                                if (vSubjectGroupParentNode.getGroup().getId().equals(parent.getId())) {
                                    // если дубликат парента-добавляем его детей к
                                    // общему списку
                                    vSubjectGroupParentNode.getChildren().add(child);
                                    // мапа парент-ребенок
                                    subjToNodeMap.put(parent.getId(), vSubjectGroupParentNode.getChildren());
                                    parentChildren.put(parent, parentSubjectGroup.getChildren());
                                    // мапа группа-ид парента
                                    mapGroupActiviti.put(parent.getsID_Group_Activiti(), parent.getId());
                                    LOG.debug("!!add mapGroupActiviti: " + parent.getsID_Group_Activiti()
                                            + "nID parent: " + parent.getId());
                                }
                            }
                        }
                    }

                }

                long stopTime_5 = System.currentTimeMillis();
                long elapsedTime_5 = stopTime_5 - startTime_5;
                LOG.info("block 5 time  {}", elapsedTime_5);

                long startTime_6 = System.currentTimeMillis();
                // достаем ид sID_Group_Activiti которое на вход
                LOG.debug("sID_Group_Activiti for tree is {}", sID_Group_Activiti_Split);
                LOG.debug("mapGroupActiviti {}", mapGroupActiviti);
                Long groupFiltr = mapGroupActiviti.get(sID_Group_Activiti_Split);

                long stopTime_6_1 = System.currentTimeMillis();
                long elapsedTime_6_1 = stopTime_6_1 - startTime_6;
                LOG.info("block 6_1 time  {}", elapsedTime_6_1);

                if (groupFiltr != null) {

                    long startTime_6_2 = System.currentTimeMillis();

                    LOG.debug("sID_Group_Activiti index: " + groupFiltr);
                    // детей его детей
                    List<SubjectGroup> children = new ArrayList<>();

                    if (isDisplayRootElement(bIncludeRoot)) {
                        SubjectGroup rootSubjectGroup = getRootSubjectGroup(parentChildren, groupFiltr);
                        children.add(rootSubjectGroup);
                    } else {
                        // детей его детей
                        children = subjToNodeMap.get(groupFiltr);
                    }
                    LOG.info("children.size: " + children.size());
                    Map<Long, List<SubjectGroup>> hierarchyProcessSubject = new HashMap<>();
                    // children полный список первого уровня

                    long stopTime_6_2 = System.currentTimeMillis();
                    long elapsedTime_6_2 = stopTime_6_2 - startTime_6_2;
                    LOG.info("block 6_2 time  {}", elapsedTime_6_2);

                    long startTime_6_3 = System.currentTimeMillis();
                    if (children != null && !children.isEmpty()) {

                        // получаем только ид чилдренов полного списка детей первого уровня
                        List<Long> idChildren = Lists
                                .newArrayList(Collections2.transform(children, new Function<SubjectGroup, Long>() {
                                    @Override
                                    public Long apply(SubjectGroup subjectGroup) {
                                        return subjectGroup.getId();
                                    }
                                }));
                        aChildResult.addAll(children);
                        LOG.info("idChildren.size: " + idChildren.size());
                        hierarchyProcessSubject = getChildrenTree(children, idChildren, subjToNodeMap, idParentList, checkDeepLevel(deepLevel), 1, aChildResult);
                        //LOG.info("hierarchyProcessSubject" + hierarchyProcessSubject);
                    }

                    long stopTime_6_3 = System.currentTimeMillis();
                    long elapsedTime_6_3 = stopTime_6_3 - startTime_6_3;
                    LOG.info("block 6_3 time  {}", elapsedTime_6_3);

                    LOG.debug("aChildResult {}", aChildResult);
                    List<SubjectGroup> aChildResultByUser = new ArrayList<>();

                    long startTime_6_4 = System.currentTimeMillis();

                    if (HUMAN.equals(sSubjectType)) {
                        aChildResultByUser = filtrChildResultByUser_New(sFind, aChildResult);
                    }
                    if (ORGAN.equals(sSubjectType)) {
                        aChildResultByUser = filtrChildResultByUser_New(sFind, aChildResult);
                    }
                    LOG.debug("aChildResultByUser {}", aChildResultByUser);

                    long stopTime_6_4 = System.currentTimeMillis();
                    long elapsedTime_6_4 = stopTime_6_4 - startTime_6_4;
                    LOG.info("block 6_4 time  {}", elapsedTime_6_4);

                    long startTime_6_5 = System.currentTimeMillis();

                    if (sFind != null && !sFind.isEmpty()) {
                        resultSubjectGroupTree = getSubjectGroupTree(hierarchyProcessSubject, aChildResultByUser, oSubjectGroupCompany);
                        //resultSubjectGroupTree = getSubjectGroupTree(hierarchyProcessSubject, aChildResultByUser);

                    } else {
                        resultSubjectGroupTree = getSubjectGroupTree(hierarchyProcessSubject, aChildResult, oSubjectGroupCompany);
                        //resultSubjectGroupTree = getSubjectGroupTree(hierarchyProcessSubject, aChildResult);
                    }
                    //LOG.info("resultTree: " + resultTree);

                    long stopTime_6_5 = System.currentTimeMillis();
                    long elapsedTime_6_5 = stopTime_6_5 - startTime_6_5;
                    LOG.info("block 6_5 time  {}", elapsedTime_6_5);
                }

                LOG.debug("resultSubjectGroupTree {}", resultSubjectGroupTree);

                long startTime_6_6 = System.currentTimeMillis();
                if (isDisplayRootElement(bIncludeRoot)) {
                    LOG.info("isDisplayRootElement");
                    if (checkDeepLevelWidth(deepLevelWidth) < resultSubjectGroupTree.size()) {
                        if (resultSubjectGroupTree != null && !resultSubjectGroupTree.isEmpty()) {
                            resultTree.add(resultSubjectGroupTree.get(checkDeepLevelWidth(deepLevelWidth).intValue()));
                        }
                    }
                } else {
                    LOG.info("isn't DisplayRootElement");
                    resultTree.addAll(resultSubjectGroupTree);
                }

                LOG.debug("resultTree is {}", resultTree);

                long stopTime_6_6 = System.currentTimeMillis();
                long elapsedTime_6_6 = stopTime_6_6 - startTime_6_6;
                LOG.info("block 6_6 time  {}", elapsedTime_6_6);

                long stopTime_6 = System.currentTimeMillis();
                long elapsedTime_6 = stopTime_6 - startTime_6;
                LOG.debug("block 6 time  {}", elapsedTime_6);
            }

            long stopTime_2 = System.currentTimeMillis();
            long elapsedTime_2 = stopTime_2 - startTime_2;
            LOG.info("Iteration time {}", elapsedTime_2);
        }

        processSubjectResultTree.setaSubjectGroupTree(removeDuplicates(resultTree));
        LOG.debug("processSubjectResultTree" + processSubjectResultTree);

        long stopTime_0 = System.currentTimeMillis();
        long elapsedTime_0 = stopTime_0 - startTime_0;
        LOG.info("Total elapsed time {}", elapsedTime_0);
        return processSubjectResultTree;

    }
    
    public static boolean isValidSubjectType(String sType) {
        return Stream.of(HUMAN, ORGAN, ALL).anyMatch(s -> s.equalsIgnoreCase(sType));
    }

    public List<SubjectGroup> getSubjectGroupsTreeUp(final String sID_Group_Activiti, final String sSubjectType) {
        return getSubjectGroupsTreeUp(sID_Group_Activiti, sSubjectType, null);
    }

    /**
     * Сервис для получения департамента по идентификатору группы.
     *
     * @param sID_Group_Activiti - идентификатор группы
     * @param sSubjectType - Тип выборки: Organ- иерархия в разрезе органы,
     * Human -иерархия в разрезе людей
     * @param nDeepLevel - 1: получаем родителя на уровень выше, 0: получаем
     * самого верхнего родителя, null: получаем
     * @return aSubjectGroupParent - лист который содержит в себе SubjectGroup
     * родительских департаментов
     *
     * @see SubjectGroup
     * @see SubjectGroupTree
     */
    public List<SubjectGroup> getSubjectGroupsTreeUp(final String sID_Group_Activiti, final String sSubjectType, final Long nDeepLevel) {
        List<SubjectGroup> aSubjectGroupResult = new ArrayList<>();

        //Получить SubjectGroup, который относятся к группе sID_Group_Activiti
        Optional<SubjectGroup> oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Group_Activiti);
        LOG.info("aSubjectGroup consist: size={}, {}", oSubjectGroup, oSubjectGroup.toString());

        LOG.info("getSubjectGroupsTreeUp nDeepLevel: {}", nDeepLevel);
        if (oSubjectGroup.isPresent()) {
            if (nDeepLevel == null || nDeepLevel == 1) {
                //ID для которого ищем департаменты, которым он подчиняется
                Long nID = oSubjectGroup.get().getId();
                //Получаем SubjectGroupTree у которых oSubjectGroup_Child равны nID
                List<SubjectGroupTree> aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Child.id", nID);
                LOG.info("aSubjectGroupTree size={}, {}", aSubjectGroupTree.size(), aSubjectGroupTree.toString());

                for (SubjectGroupTree oSubjectGroupTree : aSubjectGroupTree) {

                    SubjectGroup oSubjectGroup_Parent = oSubjectGroupTree.getoSubjectGroup_Parent();
                    LOG.info("oSubjectGroup_Parent={}", oSubjectGroup_Parent);

                    String sSubjectGroup_ParentType = oSubjectGroupService.getSubjectType(oSubjectGroup_Parent.getsID_Group_Activiti());

                    if(sSubjectGroup_ParentType != null){
                        if (sSubjectType == null || sSubjectGroup_ParentType.equalsIgnoreCase(sSubjectType)) {
                            aSubjectGroupResult.add(oSubjectGroup_Parent);
                        }
                    }
                }
                //LOG.info("aSubjectGroupResult: " + aSubjectGroupResult.toString());
            } else if (nDeepLevel == 0) {

                String sChain = oSubjectGroup.get().getsChain();
                LOG.info("getSubjectGroupsTreeUp sChain: " + sChain);

                List<SubjectGroup> oSubjectGroupRoot = oSubjectGroupDao.findAllBy("sID_Group_Activiti", sChain);
                LOG.info("oSubjectGroupRoot size={}, {}", oSubjectGroupRoot.size(), oSubjectGroupRoot.toString());

                if (sSubjectType == null) {
                    aSubjectGroupResult.addAll(oSubjectGroupRoot);

                    for (SubjectGroup oSubjectGroupParent : oSubjectGroupRoot) {
                        List<SubjectGroupTree> aSubjectGroupRoot = oSubjectGroupTreeDao.
                                findAllBy("oSubjectGroup_Parent.id", oSubjectGroupParent.getId());
                        LOG.info("oSubjectGroup_Parent={}", aSubjectGroupRoot);

                        for (SubjectGroupTree oSubjectGroupTree : aSubjectGroupRoot) {
                            SubjectGroup oSubjcetGroupChild = oSubjectGroupTree.getoSubjectGroup_Child();

                            String sSubjectGroup_ChildType = oSubjectGroupService.getSubjectType(oSubjcetGroupChild.getsID_Group_Activiti());

                            if(sSubjectGroup_ChildType != null){
                                if (sSubjectGroup_ChildType.equalsIgnoreCase("Human")) {
                                    aSubjectGroupResult.add(oSubjcetGroupChild);
                                }
                            }
                        }
                    }
                   // LOG.info("aSubjectGroupResult: " + aSubjectGroupResult.toString());
                } else if (sSubjectType.equals("Organ")) {
                    aSubjectGroupResult.addAll(oSubjectGroupRoot);
                   // LOG.info("aSubjectGroupResult: " + aSubjectGroupResult.toString());
                } else if (sSubjectType.equals("Human")) {

                    for (SubjectGroup oSubjectGroupParent : oSubjectGroupRoot) {
                        List<SubjectGroupTree> aSubjectGroupRoot = oSubjectGroupTreeDao.
                                findAllBy("oSubjectGroup_Parent.id", oSubjectGroupParent.getId());

                        for (SubjectGroupTree oSubjectGroupTree : aSubjectGroupRoot) {
                            SubjectGroup oSubjcetGroupChild = oSubjectGroupTree.getoSubjectGroup_Child();

                            String sSubjectGroup_ChildType = oSubjectGroupService.getSubjectType(oSubjcetGroupChild.getsID_Group_Activiti());
                            if (sSubjectGroup_ChildType != null && sSubjectGroup_ChildType.equalsIgnoreCase("Human")) {
                                aSubjectGroupResult.add(oSubjcetGroupChild);
                            }
                        }
                    }
                    //LOG.info("aSubjectGroupResult: " + aSubjectGroupResult.toString());
                }
            }
        }
        return aSubjectGroupResult;
    }

//------------------------------------------------------------------------------Дополнительные методы-----------------------------------------------------------------
    /**
     * Метод построения иерархии
     *
     * @param hierarchySubjectGroup
     * @param aChildResult
     * @return List<SubjectGroup> - результирующий иерархический список
     */
    private List<SubjectGroup> getSubjectGroupTree(Map<Long, List<SubjectGroup>> hierarchySubjectGroup,
            List<SubjectGroup> aChildResult, SubjectGroup oSubjectGroupCompany) {
        LOG.info("oSubjectGroupCompany in getSubjectGroupTree: {}", oSubjectGroupCompany);

        for (SubjectGroup subjectGroup : aChildResult) {
            subjectGroup.setaUser(oSubjectGroupService.getSubjectUserByActivitiGroup(subjectGroup.getsID_Group_Activiti()));

            String sCompanyName = "";
            if (oSubjectGroupCompany != null) {
                sCompanyName = oSubjectGroupCompany.getName();
            }
            subjectGroup.setsSubjectGroup_Company(sCompanyName);

            //получаем по ключу лист детей и устанавливаем
            List<SubjectGroup> aChildResultByKey = hierarchySubjectGroup.get(subjectGroup.getId());
            if (aChildResultByKey != null && !aChildResultByKey.isEmpty()) {

                for (SubjectGroup oChild : aChildResultByKey){
                    oChild.setsSubjectGroup_Company(sCompanyName);
                }

                subjectGroup.setaSubjectGroup(aChildResultByKey);
            }
        }
        //LOG.info("aChildResult {}", aChildResult);
        return aChildResult;
    }


    public List<SubjectGroup> filtrChildResultByUser_New(String sFind, List<SubjectGroup> aChildResult) {
        LOG.info("filtrChildResultByUser_New started...");
        List<SubjectGroup> aChildResultByUser = new ArrayList<>();
        List<SubjectGroup> aChildResultByUser_FirstName = new ArrayList<>();
        List<SubjectGroup> aChildResultByUser_SecondName = new ArrayList<>();
        List<SubjectGroup> aChildResultByUser_LastName = new ArrayList<>();
        List<SubjectGroup> aChildResultByUser_Login = new ArrayList<>();

        if (aChildResult != null) {
            if (sFind != null && !sFind.isEmpty()) {
                for (SubjectGroup oSubjecetGroup : aChildResult) {
                   if (oSubjecetGroup.getName() != null){
                        String[] asFullName_Elem = oSubjecetGroup.getName().trim().split(" ");
                        int counter = 0;
                        LOG.info("oSubjecetGroup.getName() is {}", oSubjecetGroup.getName());
                        if(sFind.contains(" ") && oSubjecetGroup.getName().toLowerCase().startsWith(sFind.toLowerCase())){
                            LOG.info("sFind with space is {}", sFind);
                            aChildResultByUser.add(oSubjecetGroup);
                            continue;
                        }
                        
                        for(int i = 0; i < asFullName_Elem.length; i++){
                            if(asFullName_Elem[i].trim().toLowerCase().startsWith(sFind.toLowerCase())){
                                LOG.info("asFullName_Elem[i] is {}", asFullName_Elem[i]);
                                LOG.info("sFind is {}", sFind);

                                switch (counter) {
                                    case 0:
                                        aChildResultByUser_FirstName.add(oSubjecetGroup);
                                        break;
                                    case 1:
                                        aChildResultByUser_SecondName.add(oSubjecetGroup);
                                        break;
                                    case 3:
                                        aChildResultByUser_LastName.add(oSubjecetGroup);
                                        break;
                                    default:
                                        break;
                                }

                                break;
                            }

                            if(!asFullName_Elem[i].replace(" ", "").equals("")){
                                counter++;
                            }
                        }
                        
                       if (!aChildResultByUser_FirstName.contains(oSubjecetGroup)
                                || !aChildResultByUser_SecondName.contains(oSubjecetGroup)
                                || !aChildResultByUser_LastName.contains(oSubjecetGroup)) {
                           if (!Strings.isNullOrEmpty(oSubjecetGroup.getsID_Group_Activiti())) {
                               if (oSubjecetGroup.getsID_Group_Activiti().toLowerCase().contains(sFind.toLowerCase())) {
                                   aChildResultByUser_Login.add(oSubjecetGroup);
                               }
                           }
                       }
                    }
                }
            }
        }

        aChildResultByUser.addAll(aChildResultByUser_FirstName);
        aChildResultByUser.addAll(aChildResultByUser_SecondName);
        aChildResultByUser.addAll(aChildResultByUser_LastName);
        aChildResultByUser.addAll(aChildResultByUser_Login);
        return aChildResultByUser;
    }

    /**
     * проверяем входящий параметр deepLevel
     *
     * @param deepLevel
     * @return
     */
    private Long checkDeepLevel(Long deepLevel) {
        if (deepLevel == null || deepLevel.intValue() == 0) {
            return 1000L;
        }
        return deepLevel;
    }

    /**
     * Метод структуру иерархии согласно заданной глубины и группы
     *
     * @param aChildLevel результирующий список со всеми нужными нам детьми
     * @param anID_ChildLevel ид детей уровня на котором мы находимся
     * @param subjToNodeMap мапа соответствия всех ид перентов и список его
     * детей
     * @param anID_PerentAll ид всех перентов
     * @param deepLevelRequested желаемая глубина
     * @param deepLevelFact фактическая глубина
     * @param result
     * @return Map<Long, List<ProcessSubject>> - id-parent-->list child
     */
    public Map<Long, List<SubjectGroup>> getChildrenTree(List<SubjectGroup> aChildLevel, List<Long> anID_ChildLevel,
            Map<Long, List<SubjectGroup>> subjToNodeMap, Set<Long> anID_PerentAll, Long deepLevelRequested,
            int deepLevelFact, List<SubjectGroup> result) {
        List<SubjectGroup> aChildLevel_Result = new ArrayList<>();
        List<Long> anID_ChildLevel_Result = new ArrayList<>();
        if (deepLevelFact < deepLevelRequested.intValue()) {
            for (Long nID_ChildLevel : anID_ChildLevel) {
                if (anID_PerentAll.contains(nID_ChildLevel)) {
                    // достаем детей детей
                    aChildLevel_Result = subjToNodeMap.get(nID_ChildLevel);
                    if (aChildLevel_Result != null && !aChildLevel_Result.isEmpty()) {
                        // получаем только ид чилдренов
                        List<Long> anID_Child = Lists.newArrayList(
                                Collections2.transform(aChildLevel_Result, new Function<SubjectGroup, Long>() {
                                    @Override
                                    public Long apply(SubjectGroup subjectGroup) {
                                        return subjectGroup.getId();
                                    }
                                }));
                        //если anID_ChildLevel больше 1, то всех ид складываем в лист
                        anID_ChildLevel_Result.addAll(anID_Child);
                        // добавляем детей к общему списку детей
                        result.addAll(aChildLevel_Result);
                        getChildrenTreeRes.put(nID_ChildLevel, aChildLevel_Result);
                    }
                }
            }

            deepLevelFact++;
            if (deepLevelFact < deepLevelRequested.intValue()) {
                getChildrenTree(aChildLevel_Result, anID_ChildLevel_Result, subjToNodeMap, anID_PerentAll,
                        checkDeepLevel(deepLevelRequested), deepLevelFact, result);
            }

        }
        return getChildrenTreeRes;
    }

    /**
     * Проверка флага на отображение рутового елемента:
     * <p>
     * <b>если null - устанавливать true для отображения по умолчанию</b>
     *
     * @param bIncludeRoot - флаг который прихоидит на вход (true - отображаем,
     * false - нет)
     * @return bIncludeRoot - фактическое значение флага
     */
    private static boolean isDisplayRootElement(Boolean bIncludeRoot) {
        if (bIncludeRoot == null) {
            return Boolean.FALSE;
        }
        return bIncludeRoot;
    }

    /**
     * метод возвращающий значение deepLevelWidth
     *
     * @param deepLevelWidth - ширина иерархии
     * @return deepLevelWidth - возвращается 1 (берем первый елемент из листа с
     * объектами по иерархии) если на вход передали null или 0
     */
    private Long checkDeepLevelWidth(Long deepLevelWidth) {
        if (deepLevelWidth == null || deepLevelWidth.intValue() == 0 || deepLevelWidth.intValue() == 1) {
            return 0L;
        }
        return deepLevelWidth - 1;
    }

    /**
     * Метод получения списка рутового елемента иерархии
     *
     * @param parentChildren - список парентов
     * @param groupFiltr - ид, по которому строится иерархия
     * @return ProcessSubject - рутовый елемент
     */
    private SubjectGroup getRootSubjectGroup(Map<SubjectGroup, List<SubjectGroup>> parentChildren,
            Long groupFiltr) {

        SubjectGroup rootElement = null;
        for (Map.Entry<SubjectGroup, List<SubjectGroup>> entry : parentChildren.entrySet()) {
            rootElement = entry.getKey();
            if (rootElement.getId().equals(groupFiltr)) {
                return rootElement;
            }
        }
        return rootElement;
    }

    

    /**
     * Removes duplicates from resultTree based on sID_Group_Activiti
     *
     * @param resultTree - list with possible duplicates
     * @return aoResultList - list of unique objects
     */
    private List<SubjectGroup> removeDuplicates(List<SubjectGroup> resultTree) {
        LOG.info("List<SubjectGroup> size before removeDuplicates() is " + resultTree.size());
        if (resultTree.isEmpty()) {
            return resultTree;
        }
        Set<SubjectGroup> aoUniqueSet = new TreeSet<>(new Comparator<SubjectGroup>() {
            @Override
            public int compare(SubjectGroup o1, SubjectGroup o2) {
                if (o1.getsID_Group_Activiti().equals(o2.getsID_Group_Activiti())) {
                    return 0;
                }
                return 1;
            }
        });
        aoUniqueSet.addAll(resultTree);
        List<SubjectGroup> aoResultList = new ArrayList(aoUniqueSet);
        LOG.info("List<SubjectGroup> size after removeDuplicates() is " + aoResultList.size());
        return aoResultList;
    }

    public SubjectGroup getCompany(String sLogin) throws NotFoundException {
        List<SubjectGroup> aSubjectGroup = getSubjectGroupsTreeUp(sLogin, "Organ", 0L);
        //LOG.info("aSubjectGroup {}", aSubjectGroup);
        if (aSubjectGroup == null || aSubjectGroup.isEmpty()) {
            throw new NotFoundException("Can't find any Subject by login: " + sLogin);
        } else {
            return aSubjectGroup.get(0);
        }
    }


    public Map<String, List<SubjectGroup>> checkAllSubjects(int nID, int count, String sType){

         LOG.info("checkAllSubjects started...");

        List<SubjectGroup> aHierarhyOrgan = new ArrayList<>();
        List<SubjectGroup> aHierarhyHuman = new ArrayList<>();
        List<SubjectGroup> aHumanWithoutCompany = new ArrayList<>();
        List<SubjectGroup> aGroupWithoutSubject = new ArrayList<>();

        Map<String, List<SubjectGroup>> mResult = new HashMap<>();

        List<SubjectHuman> aSubjectHuman = new ArrayList<>(oSubjectHumanDao.getSubjectHumansByIdRange(nID, count));
        List<SubjectOrgan> aSubjectOrgan = new ArrayList<>(oSubjectOrganDao.getSubjectOrgansByIdRange(nID, count));

        LOG.info("aSubjectHuman size is {}", aSubjectHuman.size());
        LOG.info("aSubjectOrgan size is {}", aSubjectOrgan.size());

        if(sType.equals("Human"))
        {
            LOG.info("checkAllSubjects process Humans");
            for(SubjectHuman oSubjectHuman : aSubjectHuman)
            {
                Optional<SubjectGroup> oSubjectGroup_Human = oSubjectGroupDao.findBy("oSubject", oSubjectHuman.getoSubject());

                if(oSubjectGroup_Human.isPresent() && oSubjectGroup_Human.get() != null){
                    LOG.info("oSubjectGroup_Human {} is present", oSubjectGroup_Human.get().getName());
                    List<SubjectGroup> aSubjectGroup_Human_Hierarhy = getSubjectGroupsTreeUp(oSubjectGroup_Human.get().getsID_Group_Activiti(), "Organ", 0L);
                    List<SubjectGroup> aSubjectGroup_aHumanWithoutCompany = getSubjectGroupsTreeUp(oSubjectGroup_Human.get().getsID_Group_Activiti(), "Organ", 1L);

                    if(aSubjectGroup_Human_Hierarhy == null || aSubjectGroup_Human_Hierarhy.isEmpty())
                    {
                        aHierarhyHuman.add(oSubjectGroup_Human.get());
                    }else{
                        LOG.info("aSubjectGroup_Human_Hierarhy size is {}", aSubjectGroup_Human_Hierarhy.size());
                    }

                    if(aSubjectGroup_aHumanWithoutCompany == null || aSubjectGroup_aHumanWithoutCompany.isEmpty())
                    {
                        aHumanWithoutCompany.add(oSubjectGroup_Human.get());
                    }else{
                        LOG.info("aSubjectGroup_aHumanWithoutCompany size is {}", aSubjectGroup_aHumanWithoutCompany.size());
                    }

                }else{
                    SubjectGroup oSubjectGroup_In_Error = new SubjectGroup();
                    oSubjectGroup_In_Error.setoSubject(oSubjectHuman.getoSubject());
                    aGroupWithoutSubject.add(oSubjectGroup_In_Error);
                }
            }
        }

        if(sType.equals("Organ"))
        {
            for(SubjectOrgan oSubjectOrgan : aSubjectOrgan)
            {
                Optional<SubjectGroup> oSubjectGroup_Organ = oSubjectGroupDao.findBy("oSubject", oSubjectOrgan.getoSubject());

                if(oSubjectGroup_Organ.isPresent() && oSubjectGroup_Organ.get() != null){
                    List<SubjectGroup> aSubjectGroup_Organ_Hierarhy = getSubjectGroupsTreeUp(oSubjectGroup_Organ.get().getsID_Group_Activiti(), "Organ", 0L);

                    if(aSubjectGroup_Organ_Hierarhy == null || aSubjectGroup_Organ_Hierarhy.isEmpty())
                    {
                        aHierarhyOrgan.add(oSubjectGroup_Organ.get());
                    }
                }else{
                    SubjectGroup oSubjectGroup_In_Error = new SubjectGroup();
                    oSubjectGroup_In_Error.setoSubject( oSubjectOrgan.getoSubject());
                    aGroupWithoutSubject.add(oSubjectGroup_In_Error);
                }
            }
        }

        mResult.put("HierarhyHuman_InError", aHierarhyHuman);
        mResult.put("HierarhyOrgan_InError", aHierarhyOrgan);
        mResult.put("Human_without_company", aHumanWithoutCompany);
        mResult.put("aGroupWithoutSubject", aGroupWithoutSubject);
        return mResult;
    }

    public SubjectGroup getHeadInDepart(SubjectGroup subjectGroup) {
        List<SubjectGroup> aoSubjectGroupEmployee = getHierarchy(HierarchyCriteria.employeesOf(subjectGroup.getsID_Group_Activiti()));
        if (aoSubjectGroupEmployee.size() == 1) {
            return aoSubjectGroupEmployee.get(0);
        }
        boolean isCompany = subjectGroup.getsID_Group_Activiti().equals(subjectGroup.getsChain());
        Set<Long> anSubjectGroupEmployee = aoSubjectGroupEmployee
                .stream()
                .map(SubjectGroup::getId)
                .collect(Collectors.toSet());
        return aoSubjectGroupEmployee
                .stream()
                .filter(sg -> oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Parent", sg)
                        .stream()
                        .map(SubjectGroupTree::getoSubjectGroup_Child)
                        .map(SubjectGroup::getId)
                        .anyMatch(anSubjectGroupEmployee::contains))
                .findFirst()
                .orElseGet(() -> isCompany ? aoSubjectGroupEmployee.stream().findFirst().orElse(null) : null);
    }

    public SubjectGroup getTopBoss(SubjectGroup subjectGroup, int level) {
        String sID_Group_Activiti = subjectGroup.getsID_Group_Activiti();
        SubjectGroup oSubjectGroupTopDepart = getHierarchy(HierarchyCriteria.parentDepartOf(sID_Group_Activiti)).stream().findFirst().orElse(null);
        if (oSubjectGroupTopDepart == null || level == 0) {
            return getHeadInDepart(subjectGroup);
        }
        List<SubjectGroup> aoSubjectGroupEmployee = getHierarchy(HierarchyCriteria.employeesOf(sID_Group_Activiti));
        List<SubjectGroup> aoSubjectGroupBoss = aoSubjectGroupEmployee
                .stream()
                .flatMap(sg -> getHierarchy(HierarchyCriteria.bossOf(sg.getsID_Group_Activiti())).stream())
                .collect(Collectors.toList());
        Set<Long> anSubjectGroupBoss = aoSubjectGroupBoss
                .stream()
                .map(SubjectGroup::getId)
                .collect(Collectors.toSet());

        return aoSubjectGroupBoss
                .stream()
                .filter(sg -> getHierarchy(HierarchyCriteria.parentDepartOf(sg.getsID_Group_Activiti()))
                        .stream()
                        .anyMatch(sID_Group_Activiti::equals))
                .filter(sg -> anSubjectGroupBoss.contains(sg.getId()))
                .findFirst()
                .orElse(getTopBoss(oSubjectGroupTopDepart, --level));
    }

    public List<SubjectGroup> getHierarchy(HierarchyCriteria oHierarchyCriteria) {
        if (!isValidSubjectType(oHierarchyCriteria.getsType())) {
            throw new RuntimeException(String.format("Specify proper 'sSubjectType' value: [%s, %s, %s]", HUMAN, ORGAN, ALL));
        }
        
        SubjectGroup oSubjectGroupRoot = oSubjectGroupService.getSubjectGroup(oHierarchyCriteria.getsRoot());
        
        if (HierarchyCriteria.LAST == oHierarchyCriteria.getnLevel()) {
            if (HierarchyCriteria.TOP == oHierarchyCriteria.getnDirection()) {
                return oSubjectGroupDao
                    .findAllBy("sID_Group_Activiti", oSubjectGroupRoot.getsChain())
                    .stream()
                    .filter(sg -> filterByType(sg, oHierarchyCriteria.getsType()))
                    .collect(Collectors.toList());
            } else {
                throw new RuntimeException("not supported: LAST BOTTOM SubjectGroup");
            }
        }
        
        List<SubjectGroup> aoResult = new ArrayList<>();
        getHierarchy(aoResult, oSubjectGroupRoot, oHierarchyCriteria);
        
        if (oHierarchyCriteria.isbIncludeRoot()) {
            aoResult.add(oSubjectGroupRoot);
        }
        return aoResult;
    }

    /**
    * recursive
    */
    private void getHierarchy(List<SubjectGroup> result, SubjectGroup root, HierarchyCriteria oHierarchyCriteria) {
        if (oHierarchyCriteria.getnLevel() == oHierarchyCriteria.getCurLevel()) {
            return;
        }
        List<SubjectGroup> aoSubjectGroup = oSubjectGroupTreeDao.findAllBy(oHierarchyCriteria.getsDirection(), root)
                .stream()
                .map(sgt -> oHierarchyCriteria.getnDirection() < 0 ? sgt.getoSubjectGroup_Child() : sgt.getoSubjectGroup_Parent())
                .filter(sg -> filterByType(sg, oHierarchyCriteria.getsType()))
                .collect(Collectors.toList());
        result.addAll(aoSubjectGroup);
        oHierarchyCriteria.up();
        aoSubjectGroup.forEach(sg -> getHierarchy(result, sg, oHierarchyCriteria));
        oHierarchyCriteria.down();
    }
    
    private boolean filterByType(SubjectGroup sg, String sType) {
        if (HUMAN.equalsIgnoreCase(sType)) {
            return oSubjectHumanService.isHuman(sg);
        } else if (ORGAN.equalsIgnoreCase(sType)) {
            return oSubjectOrganService.isOrgan(sg);
        } else if (ALL.equalsIgnoreCase(sType)) {
            return true;
        }
        return false;
    }

    public Map<Integer, List<SubjectGroup>> getHierarchyMap(HierarchyCriteria oHierarchyCriteria) {
        Map<Integer, List<SubjectGroup>> aoResult = new HashMap<>();
        SubjectGroup oSubjectGroupRoot = subjectGroupDao.findByExpected("sID_Group_Activiti", oHierarchyCriteria.getsRoot());
        aoResult.put(0, Lists.newArrayList(oSubjectGroupRoot));
        getHierarchyMap(aoResult, oSubjectGroupRoot, oHierarchyCriteria);
        return aoResult;
    }

    private void getHierarchyMap(Map<Integer, List<SubjectGroup>> result, SubjectGroup root, HierarchyCriteria oHierarchyCriteria) {
        if (oHierarchyCriteria.getnLevel() == oHierarchyCriteria.getCurLevel()) {
            return;
        }
        List<SubjectGroup> aoSubjectGroup = oSubjectGroupTreeDao.findAllBy(oHierarchyCriteria.getsDirection(), root)
                .stream()
                .map(sgt -> oHierarchyCriteria.getnDirection() < 0 ? sgt.getoSubjectGroup_Child() : sgt.getoSubjectGroup_Parent())
                .filter(sg -> HUMAN.equalsIgnoreCase(oHierarchyCriteria.getsType()) ? oSubjectHumanService.isHuman(sg) 
                        : oSubjectOrganService.isOrgan(sg))
                .collect(Collectors.toList());
        oHierarchyCriteria.up();
        if (aoSubjectGroup.size() > 0) {
            List<SubjectGroup> aoSubjectGroupSub = result.get(oHierarchyCriteria.getCurLevel());
            if (aoSubjectGroupSub == null) {
                aoSubjectGroupSub = new ArrayList<>();
            }
            aoSubjectGroupSub.addAll(aoSubjectGroup);
            result.put(oHierarchyCriteria.getCurLevel(), aoSubjectGroupSub);
        }
        aoSubjectGroup.forEach(sg -> getHierarchyMap(result, sg, oHierarchyCriteria));
        oHierarchyCriteria.down();
    }

    public SubjectGroupTree setSubjectGroupParent(SubjectGroup oSubjectGroup, SubjectGroup oSubjectGroupParent, String type) {
        List<SubjectGroupTree> aoSubjectGroupTreeParent = oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Child", oSubjectGroup);
        if (aoSubjectGroupTreeParent.stream().noneMatch(sgt -> sgt.getoSubjectGroup_Parent().getId().equals(oSubjectGroupParent.getId()))) {
            removeSubjectGroupParent(oSubjectGroup, type);
            return saveOrUpdate(oSubjectGroup, oSubjectGroupParent);
        }
        return null;
    }

    private void removeSubjectGroupParent(SubjectGroup oSubjectGroupChild, String type) {
        List<SubjectGroupTree> aoSubjectGroupTreeParent = oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Child", oSubjectGroupChild);
        if (aoSubjectGroupTreeParent.size() > 0) {
            List<SubjectGroupTree> aoSubjectGroupTreeParentByType = new ArrayList<>();
            if (ORGAN.equalsIgnoreCase(type)) {
                aoSubjectGroupTreeParentByType = aoSubjectGroupTreeParent.stream().filter(sgt 
                        -> oSubjectOrganService.isOrgan(sgt.getoSubjectGroup_Parent())).collect(Collectors.toList());
            } else if (HUMAN.equalsIgnoreCase(type)) {
                aoSubjectGroupTreeParentByType = aoSubjectGroupTreeParent.stream().filter(sgt 
                        -> oSubjectHumanService.isHuman(sgt.getoSubjectGroup_Parent())).collect(Collectors.toList());
            }
            oSubjectGroupTreeDao.delete(aoSubjectGroupTreeParentByType);
        }
    }
    
    public Set<String> getHumanGroupByGroup(String sKey_GroupPostfix_New) {
        String sSubjectType = oSubjectGroupService.getSubjectType(sKey_GroupPostfix_New);
        LOG.info("sSubjectType in cloneRights is {}", sSubjectType);

        SubjectGroupResultTree oSubjectGroupResultTree = null;

        if (sSubjectType.equals("Organ")) {
            oSubjectGroupResultTree = getCatalogSubjectGroupsTree(sKey_GroupPostfix_New,
                    1L, null, false, 1L, HUMAN);
        }

        Set<String> asID_Group_Activiti_New = new TreeSet<>();
        if (oSubjectGroupResultTree != null) {
            List<SubjectGroup> aSubjectGroups = oSubjectGroupResultTree.getaSubjectGroupTree();
            if (aSubjectGroups == null || aSubjectGroups.isEmpty()) {
                throw new RuntimeException("aSubjectGroups=" + aSubjectGroups
                        + ". Not found any SubjectGroup by sKey_GroupPostfix_New=" + sKey_GroupPostfix_New
                        + " (sSubjectType=" + sSubjectType + ")");
            } else {
                aSubjectGroups.forEach((oSubjectGroup) -> {
                    asID_Group_Activiti_New.add(oSubjectGroup.getsID_Group_Activiti());
                });
            }
        } else {
            asID_Group_Activiti_New.add(sKey_GroupPostfix_New);
        }

        LOG.info("asID_Group_Activiti_New is {}", asID_Group_Activiti_New);
        return asID_Group_Activiti_New;
    }

    public void tempRemoveSelfReference(String sID_Group_Activiti) {
        SubjectGroup oSubjectGroup = subjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        List<SubjectGroupTree> subjectGroupTrees = oSubjectGroupTreeDao.getaSubjectGroupTreeParent(oSubjectGroup);
        List<SubjectGroupTree> subjectGroupTreesToDelete = subjectGroupTrees
                .stream()
                .filter(sgt -> sgt.getoSubjectGroup_Child().getId().equals(sgt.getoSubjectGroup_Parent().getId()))
                .collect(Collectors.toList());
        oSubjectGroupTreeDao.delete(subjectGroupTreesToDelete);
    }

    public SubjectGroupTree saveOrUpdate(SubjectGroup oChild, SubjectGroup oParent) {
        if (oChild.getId().equals(oParent.getId())) {
            LOG.warn("Attempt to save self-referencing SubjectGroupTree, id: {}", oChild.getId());
            return null;
        }
        LOG.info("save SubjectGroupTree, parent: {}, child: {}", oParent.getName(), oChild.getName());
        return oSubjectGroupTreeDao.saveOrUpdate(oChild, oParent);
    }

}
