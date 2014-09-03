
#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <netdb.h>
//using namespace std;

int main (void){
	int status;
	struct addrinfo host_info;
	struct addrinfo *host_info_list;
	
	memset (&host_info,0,sizeof host_info);
	
	host_info.ai_family = AF_UNSPEC;
	host_info.ai_socktype = SOCK_STREAM;

	status = getaddrinfo ("localhost","9899",&host_info, &host_info_list);

	if (status != 0) std::cout << "getaddrinfo error" << gai_strerror(status);
	std::cout << "hello world" << std::endl;

	return 0;
}
