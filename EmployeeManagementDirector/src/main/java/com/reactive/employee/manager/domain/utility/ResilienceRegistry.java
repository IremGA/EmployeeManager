package com.reactive.employee.manager.domain.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResilienceRegistry {
    
    private List<String> registryUriList = new ArrayList<>();
    public static final Logger Employee_Director_Logger = LoggerFactory.getLogger(ResilienceRegistry.class);
    
    @Value("${employee.manager.base.path}")
    private String employeeManagerEndPoint;
    
    
    @PostConstruct
    public void init(){
        String srUri = employeeManagerEndPoint;
        register(srUri);
        
    }

    public void register(String uri) {
        
        if(null == uri || uri.isEmpty()){
            Employee_Director_Logger.warn("uri is empty. Cannot be registered");
            return;
        }
        if(registryUriList.contains(uri)){
            return;
        }
        registryUriList.add(uri);
    }

    public String getRegistryEntryFor(String uri){
        Employee_Director_Logger.debug("getRegisteryEntryFor : {} ", uri);
        Optional<String> circuitBreakerName = registryUriList.stream().filter(
                uriPattern -> {
                    return uri.contains(uriPattern);
                }
        ).max((firstUri, secondUri) -> firstUri.length() - secondUri.length());
        
        return circuitBreakerName.orElse(uri);
    }
    
}
