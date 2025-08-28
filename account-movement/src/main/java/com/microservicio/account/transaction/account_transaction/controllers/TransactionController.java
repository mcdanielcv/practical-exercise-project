package com.microservicio.account.transaction.account_transaction.controllers;

import com.microservicio.account.transaction.account_transaction.models.ResponseVo;
import com.microservicio.account.transaction.account_transaction.models.TransactionDTO;
import com.microservicio.account.transaction.account_transaction.services.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<?> getAllTransactions() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).
                body(new ResponseVo(true, "", transactionService.getAllTransactions()));
    }

    @PostMapping
    public ResponseEntity<?> saveTransaction(@Valid @RequestBody TransactionDTO transactionVo, BindingResult result) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseVo(true, "Movimiento registrado", transactionService.saveTransaction(transactionVo)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransactionByNumTransaccion(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseVo(true, "Movimiento Eliminado", transactionService.deleteTransactionById(id)));
    }
}
