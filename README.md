# json-matcher

Specs2でJsonを比較する際の、エラーメッセージをわかりやすくする、JsonMatcherを作成しました。

```
sbt test-only CompareJsonMatchErrorMessageSpec
```

通常のマッチエラー
```
[error]    '{"string_field":"string","number_field":100,"boolean_field":false,"null_field":null,"array_field":["string",100,false,null],"object_field":{"xxx_field":"xxx","yyy_field":100}}'
[error]
[error]     is not equal to
[error]
[error]    '{"number_field":100,"string_field":"string","boolean_field":false,"null_field":null,"object_field":{"xxx_field":"zzz","yyy_field":100},"array_field":["string",100,false,null]}' (CompareJsonMatchErrorMessageSpec.scala:14)
[error] Actual:   {"[string]_f...":[]"s...ng[","number]_f...":[100],"...,"[array]_f...":[]"[string",100,fa]l[se,null][object]_f...":[{]"[xxx]_f...":[]"[xxx]",["yyy_]f[ield":100}]}
[error] Expected: {"[number]_f...":[100,]"s...ng[]_f...":["string"],"...,"[object]_f...":[{]"[xxx_fie]l[d":"zzz"],"[yyy]_f...":[100},]"[array]_f...":[]"[string]",[100,]f[alse,null]
```

今回作成したマッチエラー
```
[error]    Each json not match.
[error]
[error]    Path     : object_field->xxx_field
[error]    Actual   : "xxx"
[error]    Expected : "zzz"
[error]    -----
[error]    Actual detail   :
[error]    {
[error]      "array_field" : [ "string", 100, false, null ],
[error]      "boolean_field" : false,
[error]      "null_field" : null,
[error]      "number_field" : 100,
[error]      "object_field" : {
[error]        "xxx_field" : "xxx",
[error]        "yyy_field" : 100
[error]      },
[error]      "string_field" : "string"
[error]    }
[error]
[error]    Expected detail :
[error]    {
[error]      "array_field" : [ "string", 100, false, null ],
[error]      "boolean_field" : false,
[error]      "null_field" : null,
[error]      "number_field" : 100,
[error]      "object_field" : {
[error]        "xxx_field" : "zzz",
[error]        "yyy_field" : 100
[error]      },
[error]      "string_field" : "string"
[error]    }
[error]    -----
[error]
[error]    at (CompareJsonMatchErrorMessageSpec.scala:19)
```
