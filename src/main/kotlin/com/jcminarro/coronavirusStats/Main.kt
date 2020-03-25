package com.jcminarro.coronavirusStats

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

private val FILE_NAME_PATTERN = "[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]\\.csv".toRegex()
fun main(args: Array<String>) {
    val casesFile = File(args[0])

    val data = csvReader().readAllWithHeader(casesFile)
        .groupBy { it["Last_Update"]?.split("/")?.let { "'${it[0].padStart(2, '0')}/${it[1].padStart(2, '0')}'" } ?: "" }
        .entries
        .sortedBy { it.key }
        .filterNot { it.key.isBlank() }
        .map {
            it.key to it.value
                .groupBy { it["Country_Region"] }
                .map {
                    it.key to it.value.fold(Stats(0, 0)) { acc, map ->
                        acc + Stats(map["Confirmed"]?.toIntOrNull() ?: 0, map["Deaths"]?.toIntOrNull() ?: 0)
                    }
                }
                .filter { it.first in  Country.values().map { it.displayName }}
        }
        .filter { it.second.any { it.second.confirmed > 100 } }

    val days = data.map { it.first }
    val countriesStats = data.map { it.second }

    val countriesData: List<CountryData> =
        Country.values().map { country ->
        CountryData(
            country.displayName,
            country.color,
            countriesStats.map {
                it.firstOrNull { it.first == country.displayName }
                    ?.second
                    ?: Stats(0, 0)
            }
                .fold(Stats(0,0) to listOf<Stats>()) { acc, stats ->
                    (stats to (acc.second + (stats - acc.first)))
                }.second
        )
    }

    write(days, countriesData)
}

fun write(days: List<String>, data: List<CountryData>) {
    val distDir = File("dist").apply { mkdir() }
    File(distDir, "confirmed.html").writeText(createHtml("Confirmed", days, data) { it.confirmed })
    File(distDir, "death.html").writeText(createHtml("Death", days, data) { it.death })
}

fun createHtml(title: String, headers: List<String>, data: List<CountryData>, filterData: (Stats) -> Int) =
    """
            <!doctype html>
            <html>

            <head>
            	<title>$title</title>
            	<script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
            	<style>
            	canvas{
            		-moz-user-select: none;
            		-webkit-user-select: none;
            		-ms-user-select: none;
            	}
            	</style>
            </head>

            <body>
            	<div style="width:75%;">
            		<canvas id="canvas"></canvas>
            	</div>
            	<br>
            	<br>
            	<script>
            		var config = {
            			type: 'line',
            			data: {
            				labels: $headers,
            				datasets: ${data.map { """
                                {
                                    label: '${it.name}',
                                    fill: false,
                                    backgroundColor: '${it.color}',
                                    borderColor: '${it.color}',
                                    data: ${it.data.map(filterData)}
                                }""".trimIndent() }}
            			},
            			options: {
            				responsive: true,
            				title: {
            					display: true,
            					text: '$title'
            				},
            				tooltips: {
            					mode: 'index',
            					intersect: false,
            				},
            				hover: {
            					mode: 'nearest',
            					intersect: true
            				}
            			}
            		};

            		window.onload = function() {
            			var ctx = document.getElementById('canvas').getContext('2d');
            			window.myLine = new Chart(ctx, config);
            		};
            	</script>
            </body>
            </html>
        """.trimIndent()

data class CountryData(val name: String, val color: String, val data: List<Stats>)
data class Stats(val confirmed: Int, val death: Int) {
    operator fun plus(stats: Stats) = Stats(confirmed + stats.confirmed, death + stats.death)
    operator fun minus(stats: Stats) = Stats(confirmed - stats.confirmed, death - stats.death)
}

enum class Country(val displayName: String,
                   val color: String) {
    US("US", "#FF0000"),
    GERMANY("Germany", "#000000"),
    FRANCE("France", "#88FEE9"),
    SPAIN("Spain", "#A301FA"),
    ITALY("Italy", "#008332"),
    UK("United Kingdom", "#0C01FA")
}