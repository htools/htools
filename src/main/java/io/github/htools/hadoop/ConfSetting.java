package io.github.htools.hadoop;

import org.apache.hadoop.mapreduce.MRJobConfig;

/**
 *
 * @author jeroen
 */
public interface ConfSetting extends MRJobConfig {
    static final String TASKTRACKER_MAP_TASKS_MAXIMUM = "mapreduce.tasktracker.map.tasks.maximum";
}
