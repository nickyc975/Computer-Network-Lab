/*
* THIS FILE IS FOR IP FORWARD TEST
*/
#include <map>
#include "sysInclude.h"

// system support
extern void fwd_LocalRcv(char *pBuffer, int length);

extern void fwd_SendtoLower(char *pBuffer, int length, unsigned int nexthop);

extern void fwd_DiscardPkt(char *pBuffer, int type);

extern unsigned int getIpv4Address( );

// implemented by students

map<unsigned int, unsigned int> route_table;

typedef struct ipv4_header_struct {
    unsigned char ver_head_len;
    unsigned char t_o_s;
    unsigned short total_len;
    unsigned short identity;
    unsigned short fragment;
    unsigned char ttl;
    unsigned char protocol;
    unsigned short header_chksum;
    unsigned int source_addr;
    unsigned int destination_addr;
} ipv4_header;

unsigned short cal_check_sum(ipv4_header *header) {
    unsigned int check_sum = 0;
    unsigned char *header_chksum = (unsigned char *)header;

    int head_len = (header->ver_head_len & 0xF) * 4;
    for (int i = 0; i < head_len; i += 2) {
        if (i != 10) {
            check_sum = check_sum + (header_chksum[i] << 8) + header_chksum[i + 1];
        }
    }

    int overflow = (check_sum & 0xFFFF0000) >> 16;
    while (overflow != 0) {
        check_sum = (check_sum & 0xFFFF) + overflow;
        overflow = (check_sum & 0xFFFF0000) >> 16;
    }

    check_sum = (~check_sum) & 0xFFFF;
    return htons((unsigned short)check_sum);
}

void stud_Route_Init() {
    route_table.clear();
    return;
}

void stud_route_add(stud_route_msg *proute) {
    unsigned int dest = ntohl(proute->dest);
    unsigned int masklen = htonl(proute->masklen);
    unsigned int nexthop = ntohl(proute->nexthop);
    unsigned int mask = ~(0x7FFFFFFF >> (masklen - 1));
    route_table.insert(map<unsigned int, unsigned int>::value_type(dest & mask, nexthop));
	return;
}


int stud_fwd_deal(char *pBuffer, int length) {
    unsigned int ipv4_addr = getIpv4Address();
    ipv4_header *header = (ipv4_header *)pBuffer;

    if (ntohl(header->destination_addr) == ipv4_addr) {
        fwd_LocalRcv(pBuffer,length);
		return 0;
    }

    if ((int)header->ttl <= 0) {
        fwd_DiscardPkt(pBuffer,STUD_FORWARD_TEST_TTLERROR);
		return 1;
    }

    map<unsigned int, unsigned int>::iterator item = route_table.find(ntohl(header->destination_addr));
    if (item != route_table.end()) {
        char *send_buffer = (char *)malloc(length);
        memcpy(send_buffer, pBuffer, length);
        header = (ipv4_header *)send_buffer;

        header->ttl = header->ttl - 1;
        unsigned short check_sum = cal_check_sum(header);
        memcpy(&(header->header_chksum), &check_sum, sizeof(unsigned short));
        fwd_SendtoLower(send_buffer, length, item->second);
        return 0;
    } else {
        fwd_DiscardPkt(pBuffer,STUD_FORWARD_TEST_NOROUTE);
        return 1;
    }
}

