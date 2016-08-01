import matchers.JsonMatcher
import org.specs2.mutable.Specification
import play.api.libs.json.Json

/**
  * Json比較を行い、それぞれのMatcherでのエラーメッセージを見るための、わざと失敗するテスト
  */
class CompareJsonMatchErrorMessageSpec extends Specification with JsonMatcher {

  "Show match Error Message" should {

    "Default json matcher" in {
      // デフォルトのMatcherでの比較
      jsonA must be_==(jsonB)
    }

    "New json matcher" in {
      // 今回作ったMatcherでの比較
      jsonA must equalToJson(jsonB)
    }

  }

val jsonA = Json.parse(
  """
    {
      "string_field" : "string",
      "number_field" : 100,
      "boolean_field" : false,
      "null_field" : null,
      "array_field" : ["string", 100, false, null],
      "object_field" : {"xxx_field" : "xxx", "yyy_field" : 100}
    }
  """)

// jsonAから、フィールドの順番を変えつつ、xxx_fieldの値を変えている
val jsonB = Json.parse(
  """
    {
      "number_field" : 100,
      "string_field" : "string",
      "boolean_field" : false,
      "null_field" : null,
      "object_field" : {"xxx_field" : "zzz", "yyy_field" : 100},
      "array_field" : ["string", 100, false, null]
    }
  """)

}
