/*
* THIS FILE IS FOR IP TEST
*/

// system support

#include "sysInclude.h"

extern void ip_DiscardPkt(char* pBuffer,int type);

extern void ip_SendtoLower(char*pBuffer,int length);

extern void ip_SendtoUp(char *pBuffer,int length);

extern unsigned int getIpv4Address();

// implemented by students

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

int stud_ip_recv(char *pBuffer, unsigned short length)
{
    unsigned int ipv4_addr = getIpv4Address();
    ipv4_header *header = (ipv4_header *)pBuffer;

    unsigned int head_len = (header->ver_head_len & 0xF) * 4;
    unsigned int version = (header->ver_head_len & 0xF0) >> 4;

    if (version != 4) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_VERSION_ERROR);
    } else if (head_len < 20) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_HEADLEN_ERROR);
    } else if (header->ttl == 0) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_TTL_ERROR);
    } else if (header->destination_addr != htonl(ipv4_addr)) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_DESTINATION_ERROR);
    } else if (header->header_chksum != cal_check_sum(header)) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_CHECKSUM_ERROR);
    } else {
        ip_SendtoUp(pBuffer, length);
        return 0;
    }

	return 1;
}

int stud_ip_Upsend(char *pBuffer, unsigned short len, unsigned int srcAddr,
				   unsigned int dstAddr, byte protocol, byte ttl)
{
    unsigned short header_len = sizeof(ipv4_header);
    char *sendBuffer = (char *)malloc(len + header_len);
    ipv4_header *header = (ipv4_header *)sendBuffer;
    memset(sendBuffer, 0, len + header_len);

    header->ver_head_len = 0x45;
    header->ttl = (unsigned char)ttl;
    header->protocol = (unsigned char)protocol;

    unsigned short total_len = htons(header_len + len);
    memcpy(&(header->total_len), &total_len, sizeof(unsigned short));

    unsigned int source_addr = htonl(srcAddr);
    unsigned int destination_addr = htonl(dstAddr);
    memcpy(&(header->source_addr), &source_addr, sizeof(unsigned int));
    memcpy(&(header->destination_addr), &destination_addr, sizeof(unsigned int));

    unsigned short check_sum = cal_check_sum(header);
    memcpy(&(header->header_chksum), &check_sum, sizeof(unsigned short));

    memcpy(sendBuffer + header_len, pBuffer, len);
    ip_SendtoLower(sendBuffer, len + header_len);

	return 0;
}

