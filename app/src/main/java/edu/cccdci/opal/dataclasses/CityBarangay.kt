package edu.cccdci.opal.dataclasses

data class CityBarangay(
    val cityID: String = "",
    val cityName: String = "",
    val barangays: List<String> = listOf()
)
