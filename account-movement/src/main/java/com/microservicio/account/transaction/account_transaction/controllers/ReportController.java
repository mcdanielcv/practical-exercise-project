package com.microservicio.account.transaction.account_transaction.controllers;

import com.microservicio.account.transaction.account_transaction.models.ReportDTO;
import com.microservicio.account.transaction.account_transaction.models.ResponseVo;
import com.microservicio.account.transaction.account_transaction.services.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    

    @Autowired
    private AccountService accountService;


    @GetMapping
    public ResponseEntity<?> getReporteByClientRange(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> fecha,
            @RequestParam("cliente")  Long cliente) {

        try {
            if (fecha.size() != 2) {
                throw new IllegalArgumentException("Se deben proporcionar dos fechas en el rango.");
            }

            LocalDate startLocalDate = fecha.get(0);
            LocalDate endLocalDate = fecha.get(1);
            Date startDate =  Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate =  Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            List<ReportDTO> reporte = accountService.generateReportByClientDateRange(cliente,startDate, endDate);
            return new ResponseEntity<>(reporte, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generando reporte para cliente {}: {}", cliente, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseVo(false, e.getMessage()));
        }
    }
}
