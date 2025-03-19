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

# Client

The client can be used to read and write files with *cmo*.
Configure your cameo *config.xml* file by adding the *filetransfer-server* and *filetransfer-client* apps:
```
<application name="filetransfer-server" starting_time="inf" retries="0" stopping_time="20" multiple="no" restart="no" info_arg="yes" log_directory="default">
	<start executable="java" args="-jar /path/to/cameo-file-transfer-0.1.0-full.jar"/>
</application>

<application name="filetransfer-client" starting_time="inf" retries="0" stopping_time="20" multiple="no" restart="no" info_arg="yes" log_directory="default">
	<start executable="java" args="-classpath /path/to/cameo-file-transfer-0.1.0-full.jar eu.ill.cameo.filetransfer.FileTransferClient"/>
</application>
```

Use the client:
```
$ cmo exec filetransfer-client help
$ cmo exec filetransfer-client write text /local/file.txt /remote/file.txt
$ cmo exec filetransfer-client read text /remote/file.txt /local/file.txt
$ cmo exec filetransfer-client delete /remote/file.txt
$ cmo exec filetransfer-client write directory /remote/dir
```