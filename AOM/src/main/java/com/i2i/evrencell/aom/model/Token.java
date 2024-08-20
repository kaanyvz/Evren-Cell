package com.i2i.evrencell.aom.model;

import com.i2i.evrencell.aom.enumeration.TokenType;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    private Integer     tokenId;
    private String      token;
    private TokenType   tokenType;
    private boolean     expired;
    private boolean     revoked;
    private Integer     userId;
}
