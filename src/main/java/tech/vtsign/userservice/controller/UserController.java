package tech.vtsign.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

//    private final ContractService contractService;
//
//    @Operation(summary = "document")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Success",
//                    content = @Content
//            ),
//            @ApiResponse(responseCode = "403", description = "Forbidden you don't have permission to signing this contract",
//                    content = {
//                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
//                    }),
//            @ApiResponse(responseCode = "404", description = "Not found contract",
//                    content = {
//                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
//                    }),
//    })
//    @GetMapping("/signing")
//    public ResponseEntity<?> signByReceiver(@RequestParam("c") UUID contractId,
//                                            @RequestParam("r") UUID receiverId) {
//        List<Document> documents = contractService.getDocumentsByContractAndReceiver(contractId, receiverId);
//        return ResponseEntity.ok(documents);
//    }

}
