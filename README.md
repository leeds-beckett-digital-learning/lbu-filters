# lbu-filters
General purpose Java FilterReader filter originally for use in ant.

The detailed specifications of a sophisticated filter are put into an XML
file - an LBU General Filter file. This is particularly suited for filtering text files that are not
well structured like XML or JSON in a way that slightly resembles XSLT.  This file
configures the behaviour of the FilterReader.

As the filter is specified in XML one could devise an application 
specific filter schema which is converted to LBU General Filter format
with an XSLT template.

# LBU General Filter File Specification

Expressed in XML.

## Types of Element

### Processor Elements

A processor is fed one array of Strings after another, may delegate processing to nested processors and
may output text to the filter output. One processor element, when contains a boolean element which is
used for decision making.

### Boolean elements

These elements evaluate to a boolean expression based on an input string array. Some boolean
elements contain further nested boolean elements in order to perform boolean logic.

### Specialised elements

In the current developing specification there is one element, `delimiter` which is neither a processor
nor a boolean type element. It only appears nested in the tokeniser element.

## Common Processor Attributes

There are a set of attributes that can appear in processor type elements. They control
which of the strings in the input array will be passed in an array of strings as input
to nested processors.

### `from`

This attribute, if used must be an integer number. If included the input array of strings
will be shortened. The number is a zero based index into the input string array and points at
the *first* string that will be processed by the processor.

### `to`

This attribute, if used must be an integer number. If included the input array of strings
will be shortened. The number is a zero based index into the input string array and points at
the *last* string that will be processed by the processor.

### `div`

This attribute, if used must be an integer number. When deciding whether to process a string
in the input array its index is divided by this number. The mod attribute must also be used.

### `mod`

This attribute, if used must be an integer number. For each index number of elements in the 
input string array, if this number matches the result of the div operation described above then
The string will be passed on for processing by nested processor type elements.  So, for example,
if the input is `["apple","banana","carrot","date","endive"]`, `div="3"` and `mod="0"` the nested
processors will receive `["apple","date"]`.

## Lists of Elements

### Processor Elements

Specialised elements are also listed here - with the parent element they are specific to.

#### `tokenise`

Only appears as the root element. Must contain at least one `delimiter` element and one or more processor
 type element. It reads the input stream of text looking for delimiters. When a delimitor is found it
 creates a two element array of Strings. The first element contain the text from the input stream before
 a delimiter was found and the second element contains the delimitor itself. If the end of stream is found
 before a delimitor is seen then the second member of the array will be an empty string. If a delimitor
 follows on from the previous delimiter in the stream then the first element of the output string array 
 will be empty.
 
 Each content/element string pair produced is passed to all the nexted processor type elements
 in the order they appear in the specification file.
 
#### `delimitor`

Only allowed directly nested within the `tokenise` element. The `value` attribute gives the delimitor string
which must consist of one or more text characters. 

#### `choose`

A processor type element that is modelled on the choose paradigm from XSLT. This element nests one
or more `when` elements and optionally an `otherwise` element. The order of the nested `when` elements
is significant.

The choose processor asks each `when` element in turn if it wants to process the input string array.
If none of them opt to process the input and there is an `otherwise` element, that `otherwise` element
will process the input.

#### `when`

This processor can only appear nested in a `choose` element.
It must have one nested boolean type element which is passed the input string array. If that boolean
type element evaluates to true then this when clause pass the input string array to each of the
nested processor type elements.

#### `otherwise`

Must appear as the last nested processor in a `choose` element.
Has no nested boolean type elements. If none of the sibling when elements has evaluated to true
then this element will send the input string array to all its nested processor type elements.

#### `output`

Simply outputs each of the strings in the input array to the general filter output one after the other.

#### `literal`

Ignores any input strings and outputs to the stream the value of its text attribute.

#### `split`

Has two specialised attributes; `spec` and `limit`. Spec is a Java dialect regular expression and
limit, if included, is an integer number. Split only processes the first string of the input array.
It splits that string using the regular expression and the resulting array is passed on to nested
processors. If a limit is specified and the length of the output array has reached the limit, all
the remaining input is put into the last member of the output array.

#### `replace`

Has two specialised attributes; match and with. These attributes must both be provided. For every
string in the input array, a regular expression find and replace is performed. Then the output
string array will be passed to all the nested processors.

#### `pass`

This processor does nothing but if there is/are nested processor(s) the
input strings will be passed to them.

#### `upper`

This processor converts each string in the input array to upper case letters and builds an output
string array which is passed to nested processors in turn.

### Boolean Elements

#### `matches`

Has two specialised attributes - index and spec. The index attribute specifies which string in the 
input array will be tested and spec is a regular expression to use for the test. Evaluates true
if the specified string matches the regular expression.

#### `contains`

Has two specialised attributes - index and substring. The index attribute specifies which string in the 
input array will be tested and substring is the value to match. Evaluates true if the specified
string contains the specified substring.

#### `and`

Contains nested boolean type elements. Only evaluates to true if all the nested elements evaluate true.
The input string array is passed as a parameter to each of the nested boolean elements.

#### `or`

Contains nested boolean type elements. Evaluates to true if any one of the nested elements evaluate true.
The input string array is passed as a parameter to each of the nested boolean elements.

#### `not`

The input string array is passed as a parameter to one nested boolean type element and reverses
the boolean that the nested element produces.

#### `true`

Evaluates true regardless of input. May be of use in development phase as a placeholder.

#### `false`

Evaluates false regardless of input. May be of use in development phase as a placeholder.

