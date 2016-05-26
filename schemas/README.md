Schema Discussion
=================

This is a temporary file where discussion about schema can take place.

###Question Schema
This is sourced from a REDCAP intermediate format produced in current client side applications

```javascript
[{
  "order":1,
  "variable":"variable_name",
  "form":"form_name",
  "section":"section_within_form_name",
  "type":"descriptive | text | radio | select | etc.",
  "label":"The question",
  "choices":"option 1 | option 2",
  "field":"",
  "validation":"",
  "min":"",
  "max":"",
  "identifier":"",
  "branching_logic":"[variable_name] = '7'",  //only show this question if the response associated with the question in variable_name matches comparator
  "required":"",
  "alignment":"",
  "question_number":"",
  "matrix_group":"",
  "matrix_ranking":""
}]
```
