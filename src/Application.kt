package Game

//import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Text
import io.ktor.application.*
import io.ktor.http.ContentType
//import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.cio.backend.startServerConnectionPipeline
import io.ktor.server.netty.EngineMain


import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Score : Table(){
    val score = integer("Score")
}
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    var scoreList : MutableList<convertScore> = mutableListOf()

    Database.connect("jdbc:sqlite:./StudyGame.db", "org.sqlite.JDBC")
    transaction{
        SchemaUtils.createMissingTablesAndColumns(Score)
    }
    routing{
        //add multiple choice score into database
        post("StudyGame/addMCScore"){
            val paramsJsonStr = call.receiveText()

            val myScore = Json.decodeFromString<convertScore>(paramsJsonStr)

            transaction {
                Score.insert {
                    it[score] = myScore.score
                }
            }
            call.respondText("Parameters sent : $paramsJsonStr", status = HttpStatusCode.OK, contentType=ContentType.Application.Json)
        }
        //get the highest score for Multiple Choice Game
        get("StudyGame/MCHighScore"){
            scoreList = mutableListOf()
            transaction {
                val sList = Score.selectAll()
                for(s in sList){
                    scoreList.add(convertScore(s[Score.score]))
                }
            }
            var highestScore = 0
            for(i in scoreList){
                if(i.score > highestScore){
                    highestScore = i.score
                }
            }
            val scoreListJson = Json.encodeToString(highestScore)
            call.respondText("${scoreListJson}", status = HttpStatusCode.OK, contentType = ContentType.Application.Json)
        }
        //add Memory Game score into database
        post("StudyGame/addMemoryScore"){

        }
    }
}

