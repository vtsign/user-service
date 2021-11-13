package tech.vtsign.userservice.configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tech.vtsign.userservice.proxy.FeignCustomErrorDecoder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class GlobalConfiguration {
    @Bean
    public BCryptPasswordEncoder getBCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        /*
          if you are using spring security, you can get the currently logged username with following code segment.
          SecurityContextHolder.getContext().getAuthentication().getName()
         */
        return () -> Optional.ofNullable("admin");
    }

    @Bean
    public BlobContainerClient getBlobContainerClient(@Value("${azure.storage.account-name}") String accountName,
                                                      @Value("${azure.storage.account-key}") String accountKey,
                                                      @Value("${azure.storage.container-name}") String containerName) {

        String endpoint = "https://" + accountName + ".blob.core.windows.net";
        // Create a SharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        // Create a blobServiceClient
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        return blobServiceClient.getBlobContainerClient(containerName);
    }

    @Bean
    public ErrorDecoder errorDecoder() { return new FeignCustomErrorDecoder();}
}
