package com.psmicro.accounts.service;

import com.psmicro.accounts.constants.AccountsConstants;
import com.psmicro.accounts.dto.AccountsDto;
import com.psmicro.accounts.dto.CustomerDto;
import com.psmicro.accounts.entity.Accounts;
import com.psmicro.accounts.entity.Customer;
import com.psmicro.accounts.exceptions.CustomerAlreadyExistsException;
import com.psmicro.accounts.exceptions.ResourceNotFoundException;
import com.psmicro.accounts.mapper.AccountsMapper;
import com.psmicro.accounts.mapper.CustomerMapper;
import com.psmicro.accounts.repository.AccountRepository;
import com.psmicro.accounts.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService{

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;


    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());

        Optional<Customer> optionalCustomer= customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()){
            throw new CustomerAlreadyExistsException("Customer already exists with the given mobile number" + customerDto.getMobileNumber());
        }
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCreatedBy("Anonymous");
        Customer savedCustomer =  customerRepository.save(customer);
        accountRepository.save(createAccount(savedCustomer));

    }




    private Accounts createAccount(Customer customer){
        Accounts newAcount = new Accounts();
        newAcount.setCustomerId(customer.getCustomerId());
        long accountNumber = 1000000000L + new Random().nextInt(900000000);
        newAcount.setAccountNumber(accountNumber);
        newAcount.setCreatedAt(LocalDateTime.now());
        newAcount.setCreatedBy("Anonymous");
        newAcount.setAccountType(AccountsConstants.SAVINGS);
        newAcount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAcount;
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Customer", "mobileNumber",mobileNumber)
                );
        Accounts accounts = accountRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                ()->new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto!=null){
            Accounts accounts = accountRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    ()-> new ResourceNotFoundException("Account", "Account Number",accountsDto.getAccountNumber().toString() )
            );
            AccountsMapper.mapToAccounts(accountsDto,accounts); // all the details we are getting from end user in
            // accounts dto will be fetched from accountDto and mapped to accounts
            accounts=accountRepository.save(accounts);

            Long cutomerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(cutomerId).orElseThrow(
                    ()->new ResourceNotFoundException("Customer","CustomerId",cutomerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            isUpdated=true;
        }
        return isUpdated;

    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()->new ResourceNotFoundException("Customer","mobileNumber",mobileNumber)
        );
        accountRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }
}
