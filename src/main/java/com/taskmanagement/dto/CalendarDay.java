
package com.taskmanagement.dto;

import com.taskmanagement.entity.Task;
import java.time.LocalDate;
import java.util.List;

public class CalendarDay {

    private final LocalDate date;
    private final boolean currentMonth;
    private final List<Task> tasks;

    public CalendarDay(
            LocalDate date,
            boolean currentMonth,
            List<Task> tasks) {

        this.date = date;
        this.currentMonth = currentMonth;
        this.tasks = tasks;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isCurrentMonth() {
        return currentMonth;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
