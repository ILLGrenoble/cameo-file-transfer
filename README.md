# Supported operations

Here is the list of operations:

Operation | Type      | Path                                 | Content            | Replies
----------|-----------|--------------------------------------|--------------------|---------
read      | binary    | the remote file path to read         | -                  | status, content
read      | text      | the remote file path to read         | -                  | status, content
write     | binary    | the remote file path to write to     | binary bytes array | status
write     | text      | the remote file path to write to     | string             | status
write     | directory | the remote directory path to create  | -                  | status
delete    | -         | the remote file path to delete       | -                  | status

The operation, type and path must be written in a JSON string passed as first argument to the send request.
The content is passed as the second argument of the send request.
Example of JSON string:
```json
{
  "operation": "read",
  "type": "text",
  "path": "/remote/file.txt"
}
```
There is a single reply in case of *write* and *delete* operations and two replies in case of *read*.
The status response is "OK" or "Error" in case of error.

# Server

You can start the test server:

```
cameo-server config.xml --log-console
```

And start the transfer server:

```
cmo -p 13000 exec file-transfer-server
```

# Client


Test the client using the test server:

```
$ cmo -p 13000 exec file-transfer-client help
$ cmo -p 13000 exec file-transfer-client write text /local/file.txt /remote/file.txt
$ cmo -p 13000 exec file-transfer-client read text /remote/file.txt /local/file.txt
$ cmo -p 13000 exec file-transfer-client delete /remote/file.txt
$ cmo -p 13000 exec file-transfer-client write directory /remote/dir
```