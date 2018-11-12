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

int cal_check_sum(ipv4_header *header) {
    int i;
    unsigned int check_sum = 0;
    unsigned short *header_chksum = (unsigned short *)header;

    for (i = 0; i < (header->ver_head_len & 0xF) * 2; i++) {
        if (i != 5) {
            check_sum = check_sum + ntohs(header_chksum[i]);
        }
    }

    while (((check_sum & 0xFFFF0000) >> 16) != 0) {
        check_sum = check_sum + ((check_sum & 0xFFFF0000) >> 16);
    }

    check_sum = (~check_sum) & 0xFFFF;
    return check_sum;
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
    } else if ((int)(header->ttl) == 0) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_TTL_ERROR);
    } else if (ntohl(header->destination_addr) != ipv4_addr) {
        ip_DiscardPkt(pBuffer, STUD_IP_TEST_DESTINATION_ERROR);
    } else if (cal_check_sum(header) != header->header_chksum) {
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
    unsigned int header_len = sizeof(ipv4_header);
    char *sendBuffer = (char *)malloc(len + header_len);
    ipv4_header *header = (ipv4_header *)sendBuffer;

    header->ver_head_len = (char)((4 << 4) + header_len / 4);
    header->t_o_s = (char)0;
    header->total_len = htons((unsigned short)(header_len + len));
    header->identity = htons((unsigned short)0xF0F0);
    header->fragment = (unsigned short)0;
    header->ttl = (unsigned char)ttl;
    header->protocol = (unsigned char)protocol;
    header->source_addr = htonl(srcAddr);
    header->destination_addr = htonl(dstAddr);
    header->header_chksum = cal_check_sum(header);
    memcpy(sendBuffer + header_len, pBuffer, len);
    ip_SendtoLower(sendBuffer, len + header_len);

	return 0;
}
