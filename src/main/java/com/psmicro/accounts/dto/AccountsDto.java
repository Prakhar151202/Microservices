package com.psmicro.accounts.dto;


import com.psmicro.accounts.entity.Accounts;
import lombok.*;

@Data

public class AccountsDto {

    private Long accountNumber;
    private String accountType;
    private String branchAddress;

}
