package com.example.discovery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportStateMap {

    public long offset;
    public String type;
    public Date lastUpdated;
}
