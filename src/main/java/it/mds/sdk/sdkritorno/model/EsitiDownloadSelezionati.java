/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.sdkritorno.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class EsitiDownloadSelezionati {
    private final List<String> esitoList;
    private final List<String> downloadList;

    public EsitiDownloadSelezionati(List<String> esitoList, List<String> downloadList) {

        this.esitoList = esitoList;
        this.downloadList = downloadList;
    }
}
