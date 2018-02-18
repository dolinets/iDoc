package org.igov.model.action.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.task.TaskInfo;

/**
 *
 * @author idenysenko
 */
public class SortedTaskVO {
    
    private List<TaskInfo> aoListOfTasks;
    private Map<String, ? extends Object> mSortingParameters = new HashMap<>();

    public Map<String, ? extends Object> getmSortingParameters() {
        return mSortingParameters;
    }

    public void setmSortingParameters(Map<String, ? extends Object> mSortingParameters) {
        this.mSortingParameters = mSortingParameters;
    }

    public List<TaskInfo> getAoListOfTasks() {
        return aoListOfTasks;
    }

    public void setAoListOfTasks(List<TaskInfo> aoListOfTasks) {
        this.aoListOfTasks = aoListOfTasks;
    }

}
