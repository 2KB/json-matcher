package matchers

import org.specs2.mutable.Specification
import play.api.libs.json.{JsArray, JsObject, Json}

class JsonMatcherSpec extends Specification with JsonMatcher {

  sequential

  "equalToJson" >> {

    "createKoMessageForEqualToJsonDetail" should {

      val koMessage = createKoMessageForEqualToJsonDetail _

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
        """).as[JsObject]

      "Objectの順番が違っているだけならKOメッセージが作成されないこと" >> {

        val jsonB = Json.parse(
          """
            {
              "number_field" : 100,
              "object_field" : {"yyy_field" : 100, "xxx_field" : "xxx"},
              "string_field" : "string",
              "boolean_field" : false,
              "null_field" : null,
              "array_field" : ["string", 100, false, null]
            }
          """).as[JsObject]

        koMessage(jsonA, jsonB, false) must beNone
        koMessage(jsonB, jsonA, false) must beNone

        // 念のため matcherでも true になることを試す
        jsonA must equalToJson(jsonB)
      }

      "フィールド値が異なる場合はKOメッセージが作成されること" >> {
        // 各フィールドの値をそれぞれ変えてテスト
        koMessage(jsonA, jsonA ++ Json.obj("string_field" -> "string_x"), false) must beSome.which { m =>
          m must contain("""Path     : string_field""")
          m must contain("""Actual   : "string"""")
          m must contain("""Expected : "string_x"""")
        }
        koMessage(jsonA, jsonA ++ Json.obj("number_field" -> 101), false) must beSome.which { m =>
          m must contain("""Path     : number_field""")
          m must contain("""Actual   : 100""")
          m must contain("""Expected : 101""")
        }
        koMessage(jsonA, jsonA ++ Json.obj("boolean_field" -> true), false) must beSome.which { m =>
          m must contain("""Path     : boolean_field""")
          m must contain("""Actual   : false""")
          m must contain("""Expected : true""")
        }
        koMessage(jsonA, jsonA ++ Json.obj("null_field" -> ""), false) must beSome.which { m =>
          m must contain("""Path     : null_field""")
          m must contain("""Actual   : null""")
          m must contain("""Expected : """")
        }
        koMessage(jsonA, jsonA ++ Json.obj("array_field" -> Json.parse("""["string", 101, false, null]""")), false) must beSome.which { m =>
          m must contain("""Path     : array_field->array(1)""")
          m must contain("""Actual   : 100""")
          m must contain("""Expected : 101""")
        }
        koMessage(jsonA, jsonA ++ Json.obj("object_field" -> Json.parse("""{"xxx_field" : "xxx", "yyy_field" : 101}""")), false) must beSome.which { m =>
          m must contain("""Path     : object_field->yyy_field""")
          m must contain("""Actual   : 100""")
          m must contain("""Expected : 101""")
        }
        // 各フィールドの値ではなく、型自体が違う場合
        koMessage(jsonA, jsonA ++ Json.obj("number_field" -> "100"), false) must beSome.which { m =>
          m must contain("""Path     : number_field""")
          m must contain("""Actual   : 100""")
          m must contain("""Expected : "100"""")
        }
        // 念のため matcherでも false になることを試す
        jsonA must not be equalToJson(jsonA ++ Json.obj("string_field" -> 100))
      }

      "フィールドの要素が異なる場合はKOメッセージが作成されること" >> {
        // expected のフィールドが多い
        koMessage(jsonA, jsonA ++ Json.obj("add_field" -> "add"), false) must beSome.which { m =>
          m must contain("""Path     : add_field""")
          m must contain("""Actual   : null""")
          m must contain("""Expected : "add"""")
        }
        // expected のフィールドが少ない
        koMessage(jsonA, jsonA - "array_field", false) must beSome.which { m =>
          m must contain("""Path     : array_field""")
          m must contain("""Actual   : ["string",100,false,null]""")
          m must contain("""Expected : null""")
        }
        // expected のフィールドが異なる
        koMessage(jsonA, jsonA ++ Json.obj("add_field" -> "add") - "array_field", false) must beSome.which { m =>
          m must contain("""Path     : add_field""")
          m must contain("""Actual   : null""")
          m must contain("""Expected : "add"""")
        }
        // expected の全体が空
        koMessage(jsonA, Json.parse("{}"), false) must beSome.which { m =>
          m must contain("""Path     : array_field""")
          m must contain("""Actual   : ["string",100,false,null]""")
          m must contain("""Expected : null""")
        }
        // expected のobject_fieldフィールドの、フィールドが多い
        koMessage(jsonA, jsonA ++ Json.obj("object_field" -> Json.parse("""{"xxx_field" : "xxx", "yyy_field" : 100, "zzz_field" : true}""")), false) must beSome.which { m =>
          m must contain("""Path     : object_field->zzz_field""")
          m must contain("""Actual   : null""")
          m must contain("""Expected : true""")
        }
        // 念のため matcherでも false になることを試す
        jsonA must not be equalToJson(jsonA ++ jsonA ++ Json.obj("object_field" -> Json.parse("""{"xxx_field" : "xxx", "yyy_field" : 100, "zzz_field" : true}""")))
      }

      "配列の並び順が異なる場合はKOメッセージが作成されること" >> {
        // 配列フィールドの並び順が異なる
        koMessage(jsonA, jsonA ++ Json.obj("array_field" -> Json.parse("""["string", 100, null, false]""")), false) must beSome.which { m =>
          m must contain("""Path     : array_field->array(2)""")
          m must contain("""Actual   : false""")
          m must contain("""Expected : null""")
        }
        // Json全体が配列となっていて、その配列の並び順が異なる
        val jsonArrayA = JsArray(Seq(
          jsonA ++ Json.obj("string_field" -> "string 1"),
          jsonA ++ Json.obj("string_field" -> "string 2"),
          jsonA ++ Json.obj("string_field" -> "string 3"))
        )
        val jsonArrayB = JsArray(Seq(
          jsonA ++ Json.obj("string_field" -> "string 1"),
          jsonA ++ Json.obj("string_field" -> "string 3"),
          jsonA ++ Json.obj("string_field" -> "string 2"))
        )
        koMessage(jsonArrayA, jsonArrayB, false) must beSome.which { m =>
          m must contain("""Path     : array(1)->string_field""")
          m must contain("""Actual   : "string 2"""")
          m must contain("""Expected : "string 3"""")
        }
        // 念のため matcherでも false になることを試す
        jsonArrayA must not be equalToJson(jsonArrayB)
      }

      "ネストした項目で値が異なってもKOメッセージが作成されること" >> {
        // ネストした項目のstring_field2の値が異なる
        val jsonNestA = Json.parse(
          """
          [
              {
                  "string_field": "string"
              },
              {
                  "string_field": "string",
                  "array_field": [
                      100,
                      {
                          "array_field2": [
                              {
                                  "number_field": 10,
                                  "string_field2": "HOGEHOGE"
                              },
                              false
                          ]
                      }
                  ],
                  "number_field": 10
              },
              {
                  "string_field": "string"
              }
          ]
          """)

        val jsonNestB = Json.parse(
          """
          [
              {
                  "string_field": "string"
              },
              {
                  "string_field": "string",
                  "array_field": [
                      100,
                      {
                          "array_field2": [
                              {
                                  "number_field": 10,
                                  "string_field2": "FUGAFUGA"
                              },
                              false
                          ]
                      }
                  ],
                  "number_field": 10
              },
              {
                  "string_field": "string"
              }
          ]
          """)

        koMessage(jsonNestA, jsonNestB, false) must beSome.which { m =>
          m must contain("""Path     : array(1)->array_field->array(1)->array_field2->array(0)->string_field2""")
          m must contain("""Actual   : "HOGEHOGE"""")
          m must contain("""Expected : "FUGAFUGA"""")
        }
        // 念のため matcherでも false になることを試す
        jsonNestA must not be equalToJson(jsonNestB)
      }
    }
  }
}
