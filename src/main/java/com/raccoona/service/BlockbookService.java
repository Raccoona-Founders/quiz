package com.raccoona.service;

import com.raccoona.dto.RawTransactionDto;
import com.raccoona.dto.ResultDto;
import com.raccoona.dto.ResultStringDto;
import com.raccoona.dto.UtxoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class BlockbookService {

    private RestTemplate restTemplate;
    private String baseUrl;

    Logger logger = LogManager.getLogger(BlockbookService.class);

    public BlockbookService(RestTemplate restTemplate,
                            @Value("${blockbook.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Set<UtxoDto> getUtxos(String address) {
        String url = baseUrl + "/api/v2/utxo/" + address;
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(url)
                .queryParam("confirmed", true);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.set("x-request-source", "desktop");
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity request = new HttpEntity(headers);
        logger.info(String.format("Get UTXO for address %s and url %s", address, builder.toUriString()));

        ResponseEntity<UtxoDto[]> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, UtxoDto[].class);

        logger.info(String.format("response: %s", responseEntity.getStatusCode()));
        Set<UtxoDto> result = Arrays.asList(responseEntity.getBody()).stream().collect(Collectors.toSet());
        logger.info(String.format("Obtained result: %s", result));
        return result;
    }

    //    0.0002011
    public BigDecimal estimateFee(int blocks) {
        String url = baseUrl + "/api/v2/estimatefee/" + blocks;
        return restTemplate.getForObject(
                url, ResultDto.class).result;
    }

    public String postTransaction(String hex) {
        String url = baseUrl + "/api/v2/sendtx/" + hex;
        return restTemplate.getForObject(
                url, ResultStringDto.class).result;
    }

    public RawTransactionDto getRawTransaction(String txid) {
        String url = baseUrl + "/api/v2/tx/" + txid;
        return restTemplate.getForObject(
                url, RawTransactionDto.class);
    }

}
