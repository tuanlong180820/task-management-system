
package com.taskmanagement.controller;

import com.taskmanagement.dto.CalendarDay;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.service.TaskService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CalendarController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public CalendarController(
            TaskService taskService,
            UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping("/calendar")
    public String showCalendar(
            @RequestParam(required = false) String month,
            Authentication authentication,
            Model model) {

        YearMonth selectedMonth;

        try {
            selectedMonth = (month == null || month.isBlank())
                    ? YearMonth.now()
                    : YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            return "redirect:/calendar";
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Không tìm thấy người dùng"));

        List<Task> tasks =
                taskService.getTasksByUser(user.getId());

        Map<LocalDate, List<Task>> tasksByDate = tasks
                .stream()
                .filter(task -> task.getDueDate() != null)
                .collect(Collectors.groupingBy(
                        Task::getDueDate));

        LocalDate firstDay = selectedMonth.atDay(1);

        LocalDate startDate = firstDay.minusDays(
                firstDay.getDayOfWeek().getValue() - 1);

        List<CalendarDay> days = new ArrayList<>();

        for (int i = 0; i < 42; i++) {
            LocalDate date = startDate.plusDays(i);

            days.add(new CalendarDay(
                    date,
                    YearMonth.from(date).equals(selectedMonth),
                    tasksByDate.getOrDefault(date, List.of())
            ));
        }

        model.addAttribute("days", days);
        model.addAttribute("monthTitle",
                "Tháng " + selectedMonth.getMonthValue()
                        + "/" + selectedMonth.getYear());

        model.addAttribute("previousMonth",
                selectedMonth.minusMonths(1).toString());

        model.addAttribute("nextMonth",
                selectedMonth.plusMonths(1).toString());

        return "calendar";
    }
}
